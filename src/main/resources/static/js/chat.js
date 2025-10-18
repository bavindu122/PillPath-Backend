// Minimal chat UI glue for PillPath customer dashboard
// - Align customer (me) messages right, pharmacy messages left
// - Only show typing indicator for pharmacy typing
// - Use current time if message timestamp is missing; avoid Invalid Date

(function () {
  const $msgs = document.getElementById('messages');
  const $typing = document.getElementById('typing');
  const $form = document.getElementById('composer');
  const $input = document.getElementById('text');

  // Parse chatId from /chat/{id}
  const m = location.pathname.match(/\/chat\/(\d+)/);
  const chatId = m ? Number(m[1]) : null;

  // Assume presence of CURRENT_USER_ID in global when authenticated
  const currentUserId = window.CURRENT_USER_ID || null;

  function fmtTime(ts) {
    try {
      // If ts falsy or invalid, fall back to now
      const d = ts ? new Date(ts) : new Date();
      if (isNaN(d.getTime())) {
        return new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      }
      return d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    } catch (_) {
      return new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    }
  }

  function renderMessage({ text, senderId, senderType, createdAt }) {
    const isMe = currentUserId && senderId === currentUserId || senderType === 'CUSTOMER';
    const side = isMe ? 'right' : 'left';
    const msg = document.createElement('div');
    msg.className = `msg ${side}`;

    const body = document.createElement('div');
    body.textContent = text || '';
    const meta = document.createElement('div');
    meta.className = 'meta';
    meta.textContent = fmtTime(createdAt);

    msg.appendChild(body);
    msg.appendChild(meta);
    $msgs.appendChild(msg);
    $msgs.scrollTop = $msgs.scrollHeight;
  }

  function setTyping(isPharmacyTyping) {
    // Only show when pharmacy is typing
    $typing.classList.toggle('show', !!isPharmacyTyping);
  }

  async function loadHistory() {
    if (!chatId) return;
    try {
      const res = await fetch(`/api/v1/chats/${chatId}/messages`);
      const obj = await res.json();
      const list = obj && obj.items ? obj.items : (obj.content || obj.messages || []);
      if (Array.isArray(list)) {
        list.forEach(m => renderMessage({
          text: m.text || m.content || m.message || '',
          senderId: m.senderId,
          senderType: m.senderType || m.userType,
          createdAt: m.createdAt || m.timestamp || m.time
        }));
      }
    } catch (e) {
      console.warn('Failed to load history', e);
    }
  }

  async function send(text) {
    if (!chatId || !text || !text.trim()) return;
    // Optimistically render as my message with current timestamp
    renderMessage({ text, senderId: currentUserId, senderType: 'CUSTOMER', createdAt: Date.now() });

    // Send to backend
    const headers = { 'Content-Type': 'application/json' };
    if (window.AUTH_TOKEN) headers['Authorization'] = 'Bearer ' + window.AUTH_TOKEN;
    try {
      await fetch(`/api/v1/chats/${chatId}/messages`, {
        method: 'POST',
        headers,
        body: JSON.stringify({ text })
      });
    } catch (e) {
      console.error('Failed to send', e);
    }
  }

  // Only show typing for pharmacy; do not show when customer is typing
  let typingTimeout;
  function onCustomerTyping() {
    // Do not display typing indicator for customer
    // Optionally, send a typing event to server if needed; omitted for now
    if (typingTimeout) clearTimeout(typingTimeout);
    typingTimeout = setTimeout(() => {
      // nothing to hide, as we never show customer typing
    }, 1000);
  }

  $form.addEventListener('submit', (e) => {
    e.preventDefault();
    const text = ($input.value || '').trim();
    if (!text) return;
    send(text);
    $input.value = '';
  });

  $input.addEventListener('input', onCustomerTyping);

  // Placeholder for pharmacy typing simulation via WebSocket/STOMP integration
  // Here we wire a simple event hook you can call when pharmacy is typing:
  window.__pillpath_setPharmacyTyping = setTyping;

  // Initial load
  loadHistory();
})();
