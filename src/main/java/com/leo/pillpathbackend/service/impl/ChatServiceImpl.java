package com.leo.pillpathbackend.service.impl;

import com.leo.pillpathbackend.dto.ChatMessageHistoryResponse;
import com.leo.pillpathbackend.dto.ChatRoomDTO;
import com.leo.pillpathbackend.dto.MessageDTO;
import com.leo.pillpathbackend.dto.StartChatRequest;
import com.leo.pillpathbackend.entity.*;
import com.leo.pillpathbackend.repository.ChatRoomRepository;
import com.leo.pillpathbackend.repository.CustomerRepository;
import com.leo.pillpathbackend.repository.MessageRepository;
import com.leo.pillpathbackend.repository.PharmacyRepository;
import com.leo.pillpathbackend.repository.PharmacyAdminRepository;
import com.leo.pillpathbackend.repository.PharmacistUserRepository;
import com.leo.pillpathbackend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final CustomerRepository customerRepository;
    private final PharmacyRepository pharmacyRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final PharmacyAdminRepository pharmacyAdminRepository;
    private final PharmacistUserRepository pharmacistUserRepository;

    @Override
    @Transactional
    public ChatRoomDTO startChat(Long customerId, StartChatRequest request) {
        // Find or create chat room
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Pharmacy pharmacy = pharmacyRepository.findById(request.getPharmacyId())
                .orElseThrow(() -> new RuntimeException("Pharmacy not found"));

        // Check if active chat room exists
        ChatRoom chatRoom = chatRoomRepository
                .findActiveByCustomerIdAndPharmacyId(customerId, request.getPharmacyId())
                .orElseGet(() -> {
                    // Create new chat room
                    ChatRoom newChatRoom = ChatRoom.builder()
                            .customer(customer)
                            .pharmacy(pharmacy)
                            .isActive(true)
                            .lastMessageAt(LocalDateTime.now())
                            .unreadCountCustomer(0)
                            .unreadCountPharmacist(0)
                            .build();
                    return chatRoomRepository.save(newChatRoom);
                });

        // If there's an initial message, create it
        if (request.getInitialMessage() != null && !request.getInitialMessage().trim().isEmpty()) {
            Message message = Message.builder()
                    .chatRoom(chatRoom)
                    .sender(customer)
                    .content(request.getInitialMessage())
                    .isRead(false)
                    .timestamp(LocalDateTime.now())
                    .build();
            messageRepository.save(message);

            chatRoom.updateLastMessageTime();
            chatRoom.incrementUnreadCount(true);
            chatRoomRepository.save(chatRoom);
        }

        return convertToDTO(chatRoom, request.getInitialMessage());
    }

    @Override
    public List<ChatRoomDTO> getMyChats(Long userId, String userType) {
        List<ChatRoom> chatRooms;

        if ("CUSTOMER".equalsIgnoreCase(userType)) {
            chatRooms = chatRoomRepository.findByCustomerId(userId);
        } else if ("PHARMACIST".equalsIgnoreCase(userType)) {
            // Show all chats for the pharmacist's pharmacy, not only those already assigned
            PharmacistUser ph = pharmacistUserRepository.findById(userId).orElse(null);
            if (ph != null && ph.getPharmacy() != null) {
                chatRooms = chatRoomRepository.findByPharmacyId(ph.getPharmacy().getId());
            } else {
                chatRooms = java.util.Collections.emptyList();
            }
        } else if ("ADMIN".equalsIgnoreCase(userType)) {
            // Treat ADMIN here as Pharmacy Admin listing their pharmacy chats
            PharmacyAdmin admin = pharmacyAdminRepository.findById(userId)
                    .orElse(null);
            if (admin != null && admin.getPharmacy() != null) {
                chatRooms = chatRoomRepository.findByPharmacyId(admin.getPharmacy().getId());
            } else {
                chatRooms = java.util.Collections.emptyList();
            }
        } else {
            throw new RuntimeException("Invalid user type");
        }

        return chatRooms.stream()
                .map(chatRoom -> {
                    String lastMessage = getLastMessage(chatRoom.getId());
                    ChatRoomDTO dto = convertToDTO(chatRoom, lastMessage);
                    // Set unread count depending on who is viewing
                    if ("CUSTOMER".equalsIgnoreCase(userType)) {
                        dto.setUnreadCount(chatRoom.getUnreadCountCustomer());
                    } else {
                        dto.setUnreadCount(chatRoom.getUnreadCountPharmacist());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ChatRoomDTO getChatRoomById(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        String lastMessage = getLastMessage(chatRoomId);
        return convertToDTO(chatRoom, lastMessage);
    }

    @Override
    public ChatMessageHistoryResponse getChatMessages(Long chatRoomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messagePage = messageRepository.findByChatRoomIdOrderByTimestampDesc(chatRoomId, pageable);

        List<MessageDTO> messageDTOs = messagePage.getContent().stream()
                .map(this::convertMessageToDTO)
                .collect(Collectors.toList());

        return ChatMessageHistoryResponse.builder()
                .chatRoomId(chatRoomId)
                .messages(messageDTOs)
                .currentPage(page)
                .totalPages(messagePage.getTotalPages())
                .totalMessages(messagePage.getTotalElements())
                .hasMore(messagePage.hasNext())
                .build();
    }

    @Override
    public int getUnreadCount(Long userId, String userType) {
        if (userId == null || userType == null) return 0;

        if ("CUSTOMER".equalsIgnoreCase(userType)) {
            List<ChatRoom> rooms = chatRoomRepository.findByCustomerId(userId);
            return rooms.stream()
                    .map(ChatRoom::getUnreadCountCustomer)
                    .filter(count -> count != null)
                    .mapToInt(Integer::intValue)
                    .sum();
        } else if ("PHARMACIST".equalsIgnoreCase(userType)) {
            PharmacistUser ph = pharmacistUserRepository.findById(userId).orElse(null);
            if (ph == null || ph.getPharmacy() == null) return 0;
            List<ChatRoom> rooms = chatRoomRepository.findByPharmacyId(ph.getPharmacy().getId());
            return rooms.stream()
                    .map(ChatRoom::getUnreadCountPharmacist)
                    .filter(count -> count != null)
                    .mapToInt(Integer::intValue)
                    .sum();
        }

        return 0;
    }

    @Override
    @Transactional
    public MessageDTO persistAndBroadcastMessage(Long chatRoomId, Long senderId, String userType, String text) {
        System.out.println("\n========== PERSIST AND BROADCAST MESSAGE ==========");
        System.out.println("ChatRoomId: " + chatRoomId);
        System.out.println("SenderId: " + senderId);
        System.out.println("UserType: " + userType);
        System.out.println("Text: " + text);
        
        // STEP 1: Validate chat room exists
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found"));
        System.out.println("✅ Chat room found: " + chatRoom.getId());
        System.out.println("   Customer ID: " + chatRoom.getCustomer().getId());
        System.out.println("   Pharmacy ID: " + chatRoom.getPharmacy().getId());

        // STEP 2: Identify and validate the sender
        User sender;
        boolean isCustomerSender;
        
        if ("CUSTOMER".equalsIgnoreCase(userType)) {
            // Customer sending message
            sender = chatRoom.getCustomer();
            if (sender == null || !sender.getId().equals(senderId)) {
                throw new IllegalArgumentException("You are not authorized to send messages in this chat");
            }
            isCustomerSender = true;
            System.out.println("✅ Sender validated as CUSTOMER: " + sender.getId() + " - " + sender.getFullName());
        } else if ("ADMIN".equalsIgnoreCase(userType) || "PHARMACY_ADMIN".equalsIgnoreCase(userType)) {
            // Pharmacy admin sending message
            PharmacyAdmin admin = pharmacyAdminRepository.findById(senderId).orElse(null);
            System.out.println("Looking for pharmacy admin with ID: " + senderId);
            if (admin == null) {
                System.out.println("❌ Pharmacy admin not found!");
                throw new IllegalArgumentException("Pharmacy admin not found");
            }
            System.out.println("Found pharmacy admin: " + admin.getId() + " - " + admin.getFullName());
            System.out.println("Admin's pharmacy ID: " + (admin.getPharmacy() != null ? admin.getPharmacy().getId() : "null"));
            System.out.println("Chat room pharmacy ID: " + chatRoom.getPharmacy().getId());
            
            if (admin.getPharmacy() == null 
                || !admin.getPharmacy().getId().equals(chatRoom.getPharmacy().getId())) {
                System.out.println("❌ Pharmacy admin not authorized - pharmacy mismatch!");
                throw new IllegalArgumentException("You are not authorized to send messages in this chat. " +
                    "Only pharmacy admins from the pharmacy associated with this chat can respond.");
            }
            sender = admin;
            isCustomerSender = false;
            System.out.println("✅ Sender validated as PHARMACY_ADMIN: " + sender.getId() + " - " + sender.getFullName());
        } else if ("PHARMACIST".equalsIgnoreCase(userType)) {
            // Pharmacist sending message
            PharmacistUser pharmacist = pharmacistUserRepository.findById(senderId).orElse(null);
            System.out.println("Looking for pharmacist with ID: " + senderId);
            if (pharmacist == null) {
                System.out.println("❌ Pharmacist not found!");
                throw new IllegalArgumentException("Pharmacist not found");
            }
            System.out.println("Found pharmacist: " + pharmacist.getId() + " - " + pharmacist.getFullName());
            System.out.println("Pharmacist's pharmacy ID: " + (pharmacist.getPharmacy() != null ? pharmacist.getPharmacy().getId() : "null"));
            System.out.println("Chat room pharmacy ID: " + chatRoom.getPharmacy().getId());
            
            if (pharmacist.getPharmacy() == null 
                || !pharmacist.getPharmacy().getId().equals(chatRoom.getPharmacy().getId())) {
                System.out.println("❌ Pharmacist not authorized - pharmacy mismatch!");
                throw new IllegalArgumentException("You are not authorized to send messages in this chat. " +
                    "Only pharmacy staff from the pharmacy associated with this chat can respond.");
            }
            sender = pharmacist;
            isCustomerSender = false;
            System.out.println("✅ Sender validated as PHARMACIST: " + sender.getId() + " - " + sender.getFullName());
        } else {
            System.out.println("❌ Invalid sender type: " + userType);
            throw new IllegalArgumentException("Invalid sender type. Only 'CUSTOMER', 'PHARMACY_ADMIN', and 'PHARMACIST' roles are allowed.");
        }

        // STEP 3: Persist message to database FIRST
        Message message = Message.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(text)
                .isRead(false)
                .timestamp(LocalDateTime.now())
                .build();
        message = messageRepository.save(message);
        System.out.println("✅ Message saved to database with ID: " + message.getId());

        // STEP 4: Update chat room metadata
        chatRoom.updateLastMessageTime();
        chatRoom.incrementUnreadCount(isCustomerSender);
        chatRoomRepository.save(chatRoom);
        System.out.println("✅ Chat room metadata updated");

        // STEP 5: Build message payload for WebSocket delivery
        // Create a comprehensive payload with all needed info
        String senderTypeStr = isCustomerSender ? "CUSTOMER" : "ADMIN";
        java.util.Map<String, Object> payload = java.util.Map.of(
                "id", message.getId(),
                "chatRoomId", chatRoomId,
                "senderId", sender.getId(),
                "senderName", sender.getFullName(),
                "senderType", senderTypeStr,
                "content", text,
                "timestamp", message.getTimestamp().toString(),
                "isRead", false
        );
        System.out.println("✅ WebSocket payload created: " + payload);

        // STEP 6: Broadcast to ONLY the participants of this specific chat room
        System.out.println("\n--- Broadcasting to participants ---");
        
        // Always deliver to the customer in this chat
        Long customerId = chatRoom.getCustomer().getId();
        String customerDestination = "customer:" + customerId;
        System.out.println("📤 Sending to customer: " + customerDestination + " -> /queue/chat/" + chatRoomId);
        messagingTemplate.convertAndSendToUser(customerDestination, "/queue/chat/" + chatRoomId, payload);
        
        // Deliver to all pharmacy admins for this pharmacy
        List<PharmacyAdmin> pharmacyAdmins = pharmacyAdminRepository.findByPharmacyId(chatRoom.getPharmacy().getId());
        System.out.println("📤 Found " + pharmacyAdmins.size() + " pharmacy admins for pharmacy " + chatRoom.getPharmacy().getId());
        for (PharmacyAdmin admin : pharmacyAdmins) {
            String adminDestination = "pharmacy_admin:" + admin.getId();
            System.out.println("   📤 Sending to pharmacy admin: " + adminDestination + " -> /queue/chat/" + chatRoomId);
            messagingTemplate.convertAndSendToUser(adminDestination, "/queue/chat/" + chatRoomId, payload);
        }
        
        // Also send to room-based topic for anyone subscribed to this specific chat room
        System.out.println("📤 Broadcasting to room topic: /topic/chat/room/" + chatRoomId);
        messagingTemplate.convertAndSend("/topic/chat/room/" + chatRoomId, payload);
        
        System.out.println("========== MESSAGE BROADCAST COMPLETE ==========\n");
        
        // STEP 7: Convert and return the message as DTO
        MessageDTO messageDTO = convertMessageToDTO(message);
        System.out.println("✅ Returning MessageDTO with content: '" + messageDTO.getContent() + "'");
        return messageDTO;
    }

    @Override
    @Transactional
    public void markChatAsRead(Long chatRoomId, Long userId, String userType) {
        System.out.println("Marking chat " + chatRoomId + " as read for user " + userId + " (type: " + userType + ")");
        
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("Chat room not found"));
        
        // Reset unread count for the appropriate side
        if ("CUSTOMER".equalsIgnoreCase(userType)) {
            chatRoom.resetUnreadCount(true);
        } else if ("ADMIN".equalsIgnoreCase(userType) || "PHARMACY_ADMIN".equalsIgnoreCase(userType) || "PHARMACIST".equalsIgnoreCase(userType)) {
            chatRoom.resetUnreadCount(false);
        }
        
        chatRoomRepository.save(chatRoom);
        System.out.println("✅ Chat marked as read, unread count reset");
    }

    

    private String getLastMessage(Long chatRoomId) {
        List<Message> messages = messageRepository.findTopByChatRoomIdOrderByTimestampDesc(chatRoomId, PageRequest.of(0,1));
        return (messages == null || messages.isEmpty()) ? null : messages.get(0).getContent();
    }

    private ChatRoomDTO convertToDTO(ChatRoom chatRoom, String lastMessage) {
        Customer customer = chatRoom.getCustomer();
        PharmacistUser pharmacist = chatRoom.getPharmacist();
        Pharmacy pharmacy = chatRoom.getPharmacy();

        return ChatRoomDTO.builder()
                .id(chatRoom.getId())
                .customerId(customer.getId())
                .customerName(customer.getFullName())
                .customerProfilePicture(customer.getProfilePictureUrl())
                .pharmacistId(pharmacist != null ? pharmacist.getId() : null)
                // Fallback to pharmacy name/logo when pharmacist is not assigned to avoid 'unknown user'
                .pharmacistName(pharmacist != null ? pharmacist.getFullName() : (pharmacy != null ? pharmacy.getName() : null))
                .pharmacistProfilePicture(pharmacist != null ? pharmacist.getProfilePictureUrl() : (pharmacy != null ? pharmacy.getLogoUrl() : null))
                .pharmacyId(pharmacy.getId())
                .pharmacyName(pharmacy.getName())
                .pharmacyLogoUrl(pharmacy.getLogoUrl())
                .isActive(chatRoom.getIsActive())
                .lastMessageAt(chatRoom.getLastMessageAt())
                .unreadCount(chatRoom.getUnreadCountCustomer())
                .lastMessage(lastMessage)
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }

    private MessageDTO convertMessageToDTO(Message message) {
        User sender = message.getSender();
        String senderType;
        
        if (sender instanceof Customer) {
            senderType = "CUSTOMER";
        } else if (sender instanceof PharmacyAdmin) {
            senderType = "ADMIN";
        } else if (sender instanceof PharmacistUser) {
            senderType = "PHARMACIST";
        } else {
            senderType = "UNKNOWN";
        }

        return MessageDTO.builder()
                .id(message.getId())
                .chatRoomId(message.getChatRoom().getId())
                .senderId(sender.getId())
                .senderName(sender.getFullName())
                .senderProfilePicture(sender.getProfilePictureUrl())
                .senderType(senderType)
                .content(message.getContent())
                .messageType(message.getMessageType())
                .mediaUrl(message.getMediaUrl())
                .isRead(message.getIsRead())
                .readAt(message.getReadAt())
                .timestamp(message.getTimestamp())
                .build();
    }
}
