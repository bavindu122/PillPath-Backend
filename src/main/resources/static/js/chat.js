// PillPath Chat Client with WebSocket Integration
// Features:
// - Real-time messaging via WebSocket (STOMP over SockJS)
// - Persistent message history loaded from database
// - Room-based message routing for privacy
// - Automatic reconnection on connection loss
// - Typing indicators
// - Message delivery confirmation

(function () {
  const $msgs = document.getElementById('messages');
  const $typing = document.getElementById('typing');
  const $form = document.getElementById('composer');
  const $input = document.getElementById('text');

  // Parse chatId from /chat/{id}
  const m = location.pathname.match(/\/chat\/(\d+)/);
  const chatId = m ? Number(m[1]) : null;

  // Get user info from global scope (set in chat.html or by your auth system)
  const currentUserId = window.CURRENT_USER_ID || null;
  const currentUserRole = window.CURRENT_USER_ROLE || 'customer';
  const authToken = window.AUTH_TOKEN || null;

  if (!currentUserId) {
    console.error('CURRENT_USER_ID not set! Please set window.CURRENT_USER_ID in your page.');
  }

  // WebSocket connection variables
  let stompClient = null;
  let isConnected = false;
  let reconnectAttempts = 0;
  const MAX_RECONNECT_ATTEMPTS = 5;

  // Track displayed message IDs to avoid duplicates
  const displayedMessageIds = new Set();

  function fmtTime(ts) {
    try {
      const d = ts ? new Date(ts) : new Date();
      if (isNaN(d.getTime())) {
        return new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      }
      return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } catch (_) {
      return new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }
  }

  function createMessageElement({ id, text, content, senderId, senderType, createdAt, timestamp }) {
    const messageText = text || content || '';
    const messageTime = createdAt || timestamp;
    
    // Determine if message is from current user
    // For customer view: show on right if senderId matches OR if we're in customer role and senderType is CUSTOMER
    // For pharmacy view: show on right if senderId matches current user
    const isMe = (senderId === currentUserId);
    const side = isMe ? 'right' : 'left';
    
    const msg = document.createElement('div');
    msg.className = `msg ${side}`;
    if (id) {
      msg.setAttribute('data-message-id', id);
    }

    const body = document.createElement('div');
    body.textContent = messageText;
    
    const meta = document.createElement('div');
    meta.className = 'meta';
    meta.textContent = fmtTime(messageTime);

    msg.appendChild(body);
    msg.appendChild(meta);
    return msg;
  }

  function renderMessage(messageData) {
    // Check if message already displayed (avoid duplicates)
    if (messageData.id && displayedMessageIds.has(messageData.id)) {
      return;
    }

    const msg = createMessageElement(messageData);
    $msgs.appendChild(msg);
    $msgs.scrollTop = $msgs.scrollHeight;

    // Track message ID
    if (messageData.id) {
      displayedMessageIds.add(messageData.id);
    }
  }

  function setTyping(isPharmacyTyping) {
    $typing.classList.toggle('show', !!isPharmacyTyping);
  }

  /**
   * Load message history from database.
   * Called on page load and when scrolling up for pagination.
   */
  async function loadHistory(page = 0) {
    if (!chatId) return;
    
    try {
      const headers = {};
      if (authToken) {
        headers['Authorization'] = 'Bearer ' + authToken;
      }

      const res = await fetch(`/api/v1/chats/${chatId}/messages?page=${page}&limit=50`, { headers });
      
      if (!res.ok) {
        console.error('Failed to load message history:', res.status);
        return;
      }

      const obj = await res.json();
      const list = obj && obj.messages ? obj.messages : (obj.items || obj.content || []);
      
      if (Array.isArray(list)) {
        // Backend returns messages in DESC order (newest first), reverse for display
        const reversedList = [...list].reverse();
        
        if (page > 0) {
          // Loading older messages - insert at the beginning
          const tempContainer = document.createElement('div');
          reversedList.forEach(m => {
            if (!m.id || !displayedMessageIds.has(m.id)) {
              const msgElement = createMessageElement({
                id: m.id,
                text: m.text || m.content || m.message || '',
                senderId: m.senderId,
                senderType: m.senderType || m.userType,
                createdAt: m.createdAt || m.timestamp || m.time
              });
              tempContainer.appendChild(msgElement);
              if (m.id) displayedMessageIds.add(m.id);
            }
          });
          
          while (tempContainer.firstChild) {
            $msgs.insertBefore(tempContainer.firstChild, $msgs.firstChild);
          }
        } else {
          // First page load - clear and add all messages
          $msgs.innerHTML = '';
          displayedMessageIds.clear();
          
          reversedList.forEach(m => {
            renderMessage({
              id: m.id,
              text: m.text || m.content || m.message || '',
              senderId: m.senderId,
              senderType: m.senderType || m.userType,
              createdAt: m.createdAt || m.timestamp || m.time
            });
          });
        }
        
        // Store pagination info
        window.chatPagination = {
          currentPage: obj.currentPage || page,
          totalPages: obj.totalPages || 1,
          hasMore: obj.hasMore || false
        };
      }
    } catch (e) {
      console.error('Failed to load history:', e);
    }
  }

  /**
   * Send a message via REST API (which persists and broadcasts).
   * The REST API handles:
   * 1. Saving message to database
   * 2. Broadcasting via WebSocket to all participants
   */
  async function send(text) {
    if (!chatId || !text || !text.trim()) return;
    
    // Optimistically render as my message
    renderMessage({
      id: null, // No ID yet for optimistic message
      text,
      senderId: currentUserId,
      senderType: 'CUSTOMER',
      createdAt: Date.now()
    });

    // Send to backend REST API
    const headers = { 'Content-Type': 'application/json' };
    if (authToken) {
      headers['Authorization'] = 'Bearer ' + authToken;
    }
    
    try {
      const response = await fetch(`/api/v1/chats/${chatId}/messages`, {
        method: 'POST',
        headers,
        body: JSON.stringify({ text })
      });
      
      if (!response.ok) {
        const result = await response.json();
        console.error('Failed to send message:', result);
        // Could show error UI here
      }
    } catch (e) {
      console.error('Failed to send message:', e);
      // Could show error UI here
    }
  }

  /**
   * Connect to WebSocket for real-time updates.
   * Uses STOMP over SockJS for reliable messaging.
   */
  function connectWebSocket() {
    if (!chatId || !currentUserId) {
      console.warn('Cannot connect WebSocket: missing chatId or currentUserId');
      return;
    }

    try {
      // Load SockJS and Stomp.js if not already loaded
      if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
        console.warn('SockJS or Stomp.js not loaded. Real-time updates disabled.');
        return;
      }

      // Connect with role and userId as query parameters (expected by backend)
      const wsUrl = `/ws/chat?role=${currentUserRole}&userId=${currentUserId}`;
      const socket = new SockJS(wsUrl);
      stompClient = Stomp.over(socket);

      // Enable debug for testing, disable in production
      // stompClient.debug = null;

      const headers = {};
      if (authToken) {
        headers['Authorization'] = 'Bearer ' + authToken;
      }

      stompClient.connect(headers, function(frame) {
        console.log('✅ WebSocket connected for chat room:', chatId);
        console.log('User:', currentUserId, 'Role:', currentUserRole);
        isConnected = true;
        reconnectAttempts = 0;

        // Subscribe to room-specific messages from topic
        stompClient.subscribe('/topic/chat/room/' + chatId, function(message) {
          try {
            const data = JSON.parse(message.body);
            console.log('📨 Received message from topic:', data);

            // Render incoming message (even if from self, as confirmation)
            renderMessage({
              id: data.id,
              text: data.content || data.text,
              senderId: data.senderId,
              senderType: data.senderType,
              createdAt: data.timestamp || data.createdAt || Date.now()
            });
          } catch (e) {
            console.error('Error processing WebSocket message:', e);
          }
        });

        // Also subscribe to user-specific queue for guaranteed delivery
        const userDestination = '/user/queue/chat/' + chatId;
        stompClient.subscribe(userDestination, function(message) {
          try {
            const data = JSON.parse(message.body);
            console.log('📬 Received message from user queue:', data);

            // Render if not already displayed
            renderMessage({
              id: data.id,
              text: data.content || data.text,
              senderId: data.senderId,
              senderType: data.senderType,
              createdAt: data.timestamp || data.createdAt || Date.now()
            });
          } catch (e) {
            console.error('Error processing user queue message:', e);
          }
        });

        // Subscribe to typing indicators
        stompClient.subscribe('/topic/chat/room/' + chatId + '/typing', function(message) {
          try {
            const data = JSON.parse(message.body);
            // Only show typing indicator if it's not from current user
            if (data.userId !== currentUserId) {
              setTyping(data.isTyping);
              
              // Auto-hide typing indicator after 3 seconds
              if (data.isTyping) {
                setTimeout(() => setTyping(false), 3000);
              }
            }
          } catch (e) {
            console.error('Error processing typing indicator:', e);
          }
        });

        // Subscribe to user-specific messages (for delivery confirmation, errors, etc.)
        if (currentUserId) {
          stompClient.subscribe('/user/queue/chat/' + chatId, function(message) {
            try {
              const data = JSON.parse(message.body);
              console.log('Received user-specific message:', data);
              
              // Could update message status (e.g., mark as delivered)
            } catch (e) {
              console.error('Error processing user message:', e);
            }
          });

          stompClient.subscribe('/user/queue/error', function(message) {
            try {
              const data = JSON.parse(message.body);
              console.error('WebSocket error:', data.error);
            } catch (e) {
              console.error('Error processing error message:', e);
            }
          });
        }

        // Notify server that we joined this room
        stompClient.send('/app/chat.join.' + chatId, {}, JSON.stringify({}));

      }, function(error) {
        console.error('WebSocket connection error:', error);
        isConnected = false;
        
        // Attempt to reconnect
        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
          reconnectAttempts++;
          const delay = Math.min(1000 * Math.pow(2, reconnectAttempts), 30000);
          console.log(`Reconnecting in ${delay}ms (attempt ${reconnectAttempts}/${MAX_RECONNECT_ATTEMPTS})...`);
          setTimeout(connectWebSocket, delay);
        } else {
          console.error('Max reconnection attempts reached. Please refresh the page.');
        }
      });

    } catch (e) {
      console.error('Failed to initialize WebSocket:', e);
    }
  }

  /**
   * Disconnect from WebSocket when leaving the page.
   */
  function disconnectWebSocket() {
    if (stompClient && isConnected) {
      // Notify server that we're leaving
      try {
        stompClient.send('/app/chat.leave.' + chatId, {}, JSON.stringify({}));
      } catch (e) {
        console.error('Error notifying leave:', e);
      }
      
      stompClient.disconnect(function() {
        console.log('WebSocket disconnected');
      });
    }
  }

  /**
   * Send typing indicator to other participants.
   */
  let typingTimeout;
  function sendTypingIndicator(isTyping) {
    if (!stompClient || !isConnected) return;

    try {
      stompClient.send('/app/chat.typing.' + chatId, {}, JSON.stringify({
        chatRoomId: chatId,
        isTyping: isTyping,
        userId: currentUserId,
        userName: 'Customer' // Could pass actual name if available
      }));
    } catch (e) {
      console.error('Error sending typing indicator:', e);
    }
  }

  function onCustomerTyping() {
    // Clear previous timeout
    if (typingTimeout) clearTimeout(typingTimeout);

    // Send typing start
    sendTypingIndicator(true);

    // Auto-stop typing after 1 second of inactivity
    typingTimeout = setTimeout(() => {
      sendTypingIndicator(false);
    }, 1000);
  }

  // Load more messages when scrolling to the top (infinite scroll)
  let isLoadingMore = false;
  $msgs.addEventListener('scroll', () => {
    if (!isLoadingMore && $msgs.scrollTop < 100 && window.chatPagination && window.chatPagination.hasMore) {
      isLoadingMore = true;
      const nextPage = (window.chatPagination.currentPage || 0) + 1;
      loadHistory(nextPage).finally(() => {
        isLoadingMore = false;
      });
    }
  });

  // Form submit handler
  $form.addEventListener('submit', (e) => {
    e.preventDefault();
    const text = ($input.value || '').trim();
    if (!text) return;
    send(text);
    $input.value = '';
    
    // Stop typing indicator
    sendTypingIndicator(false);
  });

  // Input event handler for typing indicator
  $input.addEventListener('input', onCustomerTyping);

  // Clean up on page unload
  window.addEventListener('beforeunload', () => {
    disconnectWebSocket();
  });

  // Initialize chat
  console.log('Initializing chat for room:', chatId);
  
  // Load message history first
  loadHistory().then(() => {
    console.log('Message history loaded');
    
    // Then connect WebSocket for real-time updates
    connectWebSocket();
  });

  // Expose for debugging
  window.chatDebug = {
    chatId,
    currentUserId,
    isConnected: () => isConnected,
    reconnect: connectWebSocket,
    disconnect: disconnectWebSocket
  };
})();
