/**
 * PillPath Pharmacy Admin Chat Client
 * JavaScript library for integrating pharmacy admin dashboard with chat system
 */

class PharmacyAdminChatClient {
    constructor(config) {
        this.baseUrl = config.baseUrl || 'http://localhost:8080';
        this.authToken = config.authToken;
        this.onMessageReceived = config.onMessageReceived || (() => {});
        this.onChatListUpdated = config.onChatListUpdated || (() => {});
        this.onError = config.onError || console.error;
        
        // WebSocket connection
        this.stompClient = null;
        this.isConnected = false;
    }

    /**
     * Initialize the chat client
     */
    async initialize() {
        try {
            await this.connectWebSocket();
            console.log('Pharmacy Admin Chat Client initialized successfully');
            return true;
        } catch (error) {
            this.onError('Failed to initialize chat client:', error);
            return false;
        }
    }

    /**
     * Get all chat rooms for the pharmacy admin
     */
    async getChatRooms() {
        try {
            const response = await fetch(`${this.baseUrl}/api/v1/chats/pharmacy-admin/dashboard/chats`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${this.authToken}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const chatRooms = await response.json();
            console.log('Loaded chat rooms:', chatRooms);
            
            this.onChatListUpdated(chatRooms);
            return chatRooms;
        } catch (error) {
            this.onError('Error loading chat rooms:', error);
            return [];
        }
    }

    /**
     * Get messages for a specific chat
     */
    async getChatMessages(chatId, page = 0, limit = 50) {
        try {
            const response = await fetch(
                `${this.baseUrl}/api/v1/chats/pharmacy-admin/dashboard/chats/${chatId}/messages?page=${page}&limit=${limit}`,
                {
                    method: 'GET',
                    headers: {
                        'Authorization': `Bearer ${this.authToken}`,
                        'Content-Type': 'application/json'
                    }
                }
            );

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const messageHistory = await response.json();
            console.log('Loaded messages for chat', chatId, ':', messageHistory);
            
            return messageHistory;
        } catch (error) {
            this.onError('Error loading chat messages:', error);
            return { messages: [], hasMore: false };
        }
    }

    /**
     * Send a message to a customer
     */
    async sendMessage(chatId, text) {
        try {
            const response = await fetch(
                `${this.baseUrl}/api/v1/chats/pharmacy-admin/dashboard/chats/${chatId}/messages`,
                {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${this.authToken}`,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ text })
                }
            );

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.error || 'Failed to send message');
            }

            const result = await response.json();
            console.log('Message sent successfully:', result);
            
            return result;
        } catch (error) {
            this.onError('Error sending message:', error);
            throw error;
        }
    }

    /**
     * Connect to WebSocket for real-time notifications
     */
    async connectWebSocket() {
        if (typeof SockJS === 'undefined' || typeof Stomp === 'undefined') {
            console.warn('SockJS and Stomp libraries not found. Real-time notifications disabled.');
            return;
        }

        try {
            const socket = new SockJS(`${this.baseUrl}/ws`);
            this.stompClient = Stomp.over(socket);
            
            // Add auth headers
            const headers = {};
            if (this.authToken) {
                headers['Authorization'] = `Bearer ${this.authToken}`;
            }

            return new Promise((resolve, reject) => {
                this.stompClient.connect(headers, (frame) => {
                    console.log('Connected to WebSocket:', frame);
                    this.isConnected = true;

                    // Subscribe to pharmacy admin notifications
                    this.stompClient.subscribe('/user/queue/chat', (message) => {
                        const messageData = JSON.parse(message.body);
                        console.log('Received real-time message:', messageData);
                        this.onMessageReceived(messageData);
                    });

                    resolve();
                }, (error) => {
                    console.error('WebSocket connection failed:', error);
                    this.isConnected = false;
                    reject(error);
                });
            });
        } catch (error) {
            this.onError('WebSocket connection error:', error);
            throw error;
        }
    }

    /**
     * Disconnect from WebSocket
     */
    disconnect() {
        if (this.stompClient && this.isConnected) {
            this.stompClient.disconnect(() => {
                console.log('Disconnected from WebSocket');
                this.isConnected = false;
            });
        }
    }

    /**
     * Check if connected to WebSocket
     */
    isWebSocketConnected() {
        return this.isConnected;
    }
}

// Export for use in different environments
if (typeof module !== 'undefined' && module.exports) {
    module.exports = PharmacyAdminChatClient;
} else if (typeof window !== 'undefined') {
    window.PharmacyAdminChatClient = PharmacyAdminChatClient;
}