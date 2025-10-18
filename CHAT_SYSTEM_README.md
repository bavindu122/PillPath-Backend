# PillPath Chat System Documentation

## Overview
A comprehensive real-time chat system enabling customers to communicate with pharmacists through WebSocket (STOMP) and REST APIs.

## Architecture

### Components
1. **Entities**: ChatRoom, Message
2. **Repositories**: ChatRoomRepository, MessageRepository
3. **Services**: ChatService
4. **Controllers**: ChatController (REST), WebSocketChatController (WebSocket)
5. **DTOs**: ChatRoomDTO, MessageDTO, SendMessageRequest, StartChatRequest, PharmacySearchDTO
6. **Configuration**: WebSocketConfig

---

## REST API Endpoints

### 1. Search Pharmacies
**Endpoint**: `GET /api/v1/pharmacies/search?name={pharmacyName}`

**Description**: Search for pharmacies by name to initiate a chat.

**Headers**:
```
Authorization: Bearer {jwt_token}
```

**Response**:
```json
[
  {
    "id": 1,
    "name": "HealthCare Pharmacy",
    "address": "123 Main St, City",
    "phoneNumber": "+1234567890",
    "email": "info@healthcare.com",
    "logoUrl": "https://...",
    "isVerified": true,
    "isActive": true,
    "averageRating": 4.5,
    "totalReviews": 120
  }
]
```

### 2. Start Chat
**Endpoint**: `POST /api/chats/start`

**Description**: Create a new chat room or retrieve existing one with a pharmacy.

**Headers**:
```
Authorization: Bearer {jwt_token}
Content-Type: application/json
```

**Request Body**:
```json
{
  "pharmacyId": 1,
  "initialMessage": "Hello, I have a question about my prescription"
}
```

**Response**:
```json
{
  "id": 1,
  "customerId": 5,
  "customerName": "John Doe",
  "customerProfilePicture": "https://...",
  "pharmacistId": null,
  "pharmacistName": null,
  "pharmacistProfilePicture": null,
  "pharmacyId": 1,
  "pharmacyName": "HealthCare Pharmacy",
  "pharmacyLogoUrl": "https://...",
  "isActive": true,
  "lastMessageAt": "2025-10-17T10:30:00",
  "unreadCount": 0,
  "lastMessage": "Hello, I have a question about my prescription",
  "createdAt": "2025-10-17T10:30:00"
}
```

### 3. Get My Chat Rooms
**Endpoint**: `GET /api/chats/my-chats`

**Description**: Get all chat rooms for the authenticated user (customer or pharmacist).

**Headers**:
```
Authorization: Bearer {jwt_token}
```

**Response**:
```json
[
  {
    "id": 1,
    "customerId": 5,
    "customerName": "John Doe",
    "customerProfilePicture": "https://...",
    "pharmacistId": 10,
    "pharmacistName": "Dr. Sarah Smith",
    "pharmacistProfilePicture": "https://...",
    "pharmacyId": 1,
    "pharmacyName": "HealthCare Pharmacy",
    "pharmacyLogoUrl": "https://...",
    "isActive": true,
    "lastMessageAt": "2025-10-17T10:30:00",
    "unreadCount": 3,
    "lastMessage": "Your prescription is ready for pickup",
    "createdAt": "2025-10-17T09:00:00"
  }
]
```

### 4. Get Chat Messages
**Endpoint**: `GET /api/chats/{chatId}/messages?page=0&size=50`

**Description**: Retrieve message history for a specific chat room with pagination.

**Headers**:
```
Authorization: Bearer {jwt_token}
```

**Response**:
```json
{
  "chatRoomId": 1,
  "messages": [
    {
      "id": 1,
      "chatRoomId": 1,
      "senderId": 5,
      "senderName": "John Doe",
      "senderProfilePicture": "https://...",
      "senderType": "CUSTOMER",
      "content": "Hello, I have a question",
      "messageType": "TEXT",
      "mediaUrl": null,
      "isRead": true,
      "readAt": "2025-10-17T10:31:00",
      "timestamp": "2025-10-17T10:30:00"
    },
    {
      "id": 2,
      "chatRoomId": 1,
      "senderId": 10,
      "senderName": "Dr. Sarah Smith",
      "senderProfilePicture": "https://...",
      "senderType": "PHARMACIST",
      "content": "Hello! How can I help you?",
      "messageType": "TEXT",
      "mediaUrl": null,
      "isRead": true,
      "readAt": "2025-10-17T10:32:00",
      "timestamp": "2025-10-17T10:31:00"
    }
  ],
  "currentPage": 0,
  "totalPages": 1,
  "totalMessages": 2,
  "hasMore": false
}
```

### 5. Mark Messages as Read
**Endpoint**: `PUT /api/chats/{chatId}/mark-read`

**Description**: Mark all messages in a chat room as read.

**Headers**:
```
Authorization: Bearer {jwt_token}
```

**Response**:
```json
{
  "message": "Messages marked as read"
}
```

### 6. Assign Pharmacist to Chat
**Endpoint**: `PUT /api/chats/{chatId}/assign-pharmacist?pharmacistId={pharmacistId}`

**Description**: Assign a pharmacist to handle a chat room.

**Response**:
```json
{
  "message": "Pharmacist assigned successfully"
}
```

### 7. Close Chat Room
**Endpoint**: `PUT /api/chats/{chatId}/close`

**Description**: Close/deactivate a chat room.

**Headers**:
```
Authorization: Bearer {jwt_token}
```

**Response**:
```json
{
  "message": "Chat room closed successfully"
}
```

---

## WebSocket Implementation

### Connection Setup

#### 1. Connect to WebSocket
**Endpoint**: `/ws/chat`

**JavaScript Example (using SockJS + STOMP)**:
```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

// Initialize connection
const socket = new SockJS('http://localhost:8080/ws/chat');
const stompClient = Stomp.over(socket);

// Connect with JWT authentication
stompClient.connect(
  {
    'Authorization': `Bearer ${jwtToken}`
  },
  (frame) => {
    console.log('Connected: ' + frame);
    
    // Subscribe to chat room messages
    stompClient.subscribe(`/topic/chat/${chatRoomId}`, (message) => {
      const messageData = JSON.parse(message.body);
      console.log('Received message:', messageData);
      // Handle incoming message
      displayMessage(messageData);
    });
    
    // Subscribe to notifications
    stompClient.subscribe(`/queue/notifications/${chatRoomId}`, (notification) => {
      const data = JSON.parse(notification.body);
      console.log('New notification:', data);
    });
    
    // Subscribe to typing indicators
    stompClient.subscribe(`/topic/chat/${chatRoomId}/typing`, (typingData) => {
      const userId = JSON.parse(typingData.body);
      console.log('User typing:', userId);
      showTypingIndicator(userId);
    });
    
    // Subscribe to read receipts
    stompClient.subscribe(`/topic/chat/${chatRoomId}/read`, (readData) => {
      const userId = JSON.parse(readData.body);
      console.log('Messages read by:', userId);
      updateReadStatus(userId);
    });
  },
  (error) => {
    console.error('STOMP error:', error);
  }
);
```

#### 2. Send Message
```javascript
// Send a text message
function sendMessage(chatRoomId, content) {
  const message = {
    chatRoomId: chatRoomId,
    content: content,
    messageType: 'TEXT',
    mediaUrl: null
  };
  
  stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(message));
}

// Example usage
sendMessage(1, 'Hello, I need help with my prescription');
```

#### 3. Send Typing Indicator
```javascript
function sendTypingIndicator(chatRoomId) {
  stompClient.send('/app/chat.typing', {}, chatRoomId);
}

// Example: Trigger on input field change
document.getElementById('messageInput').addEventListener('input', () => {
  sendTypingIndicator(currentChatRoomId);
});
```

#### 4. Mark Messages as Read
```javascript
function markChatAsRead(chatRoomId) {
  stompClient.send('/app/chat.markRead', {}, chatRoomId);
}

// Example: Mark as read when chat is opened
markChatAsRead(1);
```

#### 5. Disconnect
```javascript
function disconnect() {
  if (stompClient !== null) {
    stompClient.disconnect(() => {
      console.log('Disconnected');
    });
  }
}
```

---

## React/TypeScript Example

### Complete Chat Component

```typescript
import React, { useEffect, useState, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Stomp, CompatClient } from '@stomp/stompjs';

interface Message {
  id: number;
  chatRoomId: number;
  senderId: number;
  senderName: string;
  senderType: 'CUSTOMER' | 'PHARMACIST';
  content: string;
  timestamp: string;
  isRead: boolean;
}

interface ChatProps {
  chatRoomId: number;
  jwtToken: string;
  currentUserId: number;
}

const ChatComponent: React.FC<ChatProps> = ({ chatRoomId, jwtToken, currentUserId }) => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [inputMessage, setInputMessage] = useState('');
  const [isTyping, setIsTyping] = useState(false);
  const stompClient = useRef<CompatClient | null>(null);

  useEffect(() => {
    // Fetch message history
    fetchMessageHistory();

    // Connect to WebSocket
    connectWebSocket();

    return () => {
      disconnectWebSocket();
    };
  }, [chatRoomId]);

  const fetchMessageHistory = async () => {
    try {
      const response = await fetch(
        `http://localhost:8080/api/chats/${chatRoomId}/messages?page=0&size=50`,
        {
          headers: {
            'Authorization': `Bearer ${jwtToken}`
          }
        }
      );
      const data = await response.json();
      setMessages(data.messages.reverse()); // Reverse for chronological order
    } catch (error) {
      console.error('Error fetching messages:', error);
    }
  };

  const connectWebSocket = () => {
    const socket = new SockJS('http://localhost:8080/ws/chat');
    stompClient.current = Stomp.over(socket);

    stompClient.current.connect(
      { 'Authorization': `Bearer ${jwtToken}` },
      () => {
        console.log('WebSocket connected');

        // Subscribe to messages
        stompClient.current?.subscribe(`/topic/chat/${chatRoomId}`, (message) => {
          const newMessage: Message = JSON.parse(message.body);
          setMessages(prev => [...prev, newMessage]);
        });

        // Subscribe to typing indicators
        stompClient.current?.subscribe(`/topic/chat/${chatRoomId}/typing`, (data) => {
          const userId = JSON.parse(data.body);
          if (userId !== currentUserId) {
            setIsTyping(true);
            setTimeout(() => setIsTyping(false), 3000);
          }
        });
      },
      (error) => {
        console.error('WebSocket error:', error);
      }
    );
  };

  const disconnectWebSocket = () => {
    if (stompClient.current) {
      stompClient.current.disconnect();
    }
  };

  const sendMessage = () => {
    if (!inputMessage.trim() || !stompClient.current?.connected) return;

    const message = {
      chatRoomId: chatRoomId,
      content: inputMessage,
      messageType: 'TEXT',
      mediaUrl: null
    };

    stompClient.current.send('/app/chat.sendMessage', {}, JSON.stringify(message));
    setInputMessage('');
  };

  const handleTyping = () => {
    if (stompClient.current?.connected) {
      stompClient.current.send('/app/chat.typing', {}, chatRoomId.toString());
    }
  };

  return (
    <div className="chat-container">
      <div className="messages">
        {messages.map((msg) => (
          <div 
            key={msg.id} 
            className={msg.senderId === currentUserId ? 'message-sent' : 'message-received'}
          >
            <div className="message-sender">{msg.senderName}</div>
            <div className="message-content">{msg.content}</div>
            <div className="message-time">
              {new Date(msg.timestamp).toLocaleTimeString()}
            </div>
          </div>
        ))}
        {isTyping && <div className="typing-indicator">Typing...</div>}
      </div>
      
      <div className="message-input">
        <input
          type="text"
          value={inputMessage}
          onChange={(e) => {
            setInputMessage(e.target.value);
            handleTyping();
          }}
          onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
          placeholder="Type a message..."
        />
        <button onClick={sendMessage}>Send</button>
      </div>
    </div>
  );
};

export default ChatComponent;
```

---

## Database Schema

### ChatRoom Table
```sql
CREATE TABLE chat_rooms (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL REFERENCES users(id),
    pharmacist_id BIGINT REFERENCES users(id),
    pharmacy_id BIGINT NOT NULL REFERENCES pharmacies(id),
    is_active BOOLEAN DEFAULT true,
    last_message_at TIMESTAMP,
    unread_count_customer INTEGER DEFAULT 0,
    unread_count_pharmacist INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
```

### Message Table
```sql
CREATE TABLE messages (
    id BIGSERIAL PRIMARY KEY,
    chat_room_id BIGINT NOT NULL REFERENCES chat_rooms(id),
    sender_id BIGINT NOT NULL REFERENCES users(id),
    content VARCHAR(2000) NOT NULL,
    is_read BOOLEAN DEFAULT false,
    read_at TIMESTAMP,
    message_type VARCHAR(50) DEFAULT 'TEXT',
    media_url VARCHAR(500),
    timestamp TIMESTAMP NOT NULL
);

CREATE INDEX idx_chat_room_timestamp ON messages(chat_room_id, timestamp DESC);
```

---

## Usage Flow

### For Customers:
1. Search for a pharmacy using `/api/v1/pharmacies/search?name={name}`
2. Start a chat with `/api/chats/start` providing pharmacyId
3. Connect to WebSocket with JWT token
4. Subscribe to `/topic/chat/{chatRoomId}`
5. Send messages via `/app/chat.sendMessage`
6. Receive real-time responses

### For Pharmacists:
1. Retrieve chat rooms using `/api/chats/my-chats`
2. Assign themselves to unassigned chats (optional)
3. Connect to WebSocket
4. Subscribe to all their active chat rooms
5. Respond to customer messages in real-time

---

## Security Features

1. **JWT Authentication**: All REST endpoints require valid JWT token
2. **WebSocket Auth**: JWT validated during WebSocket handshake
3. **Access Control**: Users can only access their own chat rooms
4. **Message Validation**: Content validated before saving

---

## Testing with Postman/cURL

### 1. Search Pharmacies
```bash
curl -X GET "http://localhost:8080/api/v1/pharmacies/search?name=Health" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 2. Start Chat
```bash
curl -X POST "http://localhost:8080/api/chats/start" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "pharmacyId": 1,
    "initialMessage": "Hello, I need assistance"
  }'
```

### 3. Get Messages
```bash
curl -X GET "http://localhost:8080/api/chats/1/messages?page=0&size=50" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Troubleshooting

### WebSocket Connection Issues
- Ensure CORS is properly configured
- Check that JWT token is valid and not expired
- Verify WebSocket endpoint is accessible (no firewall blocking)
- Check browser console for connection errors

### Messages Not Saving
- Verify database connection
- Check that user has access to the chat room
- Ensure message content is not empty
- Check server logs for exceptions

### Real-time Updates Not Working
- Confirm WebSocket connection is active
- Verify subscription to correct topic
- Check that message format matches DTO structure
- Ensure STOMP protocol is properly configured

---

## Performance Considerations

1. **Pagination**: Messages are paginated (default 50 per page)
2. **Indexing**: Database indexes on chat_room_id and timestamp
3. **Connection Pooling**: Configure appropriate database connection pool size
4. **Message Size**: Limited to 2000 characters per message
5. **Lazy Loading**: Entities use LAZY fetch to optimize queries

---

## Future Enhancements

- [ ] File/Image sharing
- [ ] Message editing and deletion
- [ ] Delivery and read receipts
- [ ] Push notifications
- [ ] Chat history export
- [ ] Emoji support
- [ ] Voice messages
- [ ] Video call integration
- [ ] Chat bots for common queries
- [ ] Message search functionality

---

## Support

For issues or questions, please contact the development team or refer to the main project documentation.

