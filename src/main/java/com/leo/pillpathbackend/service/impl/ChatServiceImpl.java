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
import com.leo.pillpathbackend.ws.WatchRegistry;
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
    private final WatchRegistry watchRegistry;
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
    public void persistAndBroadcastMessage(Long chatRoomId, Long senderId, String userType, String text) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("chat not found"));

        User sender;
        boolean isCustomerSender;
        if ("CUSTOMER".equalsIgnoreCase(userType)) {
            sender = chatRoom.getCustomer();
            if (sender == null || !sender.getId().equals(senderId)) throw new IllegalArgumentException("not your chat");
            isCustomerSender = true;
        } else if ("ADMIN".equalsIgnoreCase(userType) || "PHARMACIST".equalsIgnoreCase(userType)) {
            // Resolve the actual pharmacy-side sender entity
            sender = null;
            if ("PHARMACIST".equalsIgnoreCase(userType)) {
                // If chat has assigned pharmacist and matches, use it; otherwise try to load by senderId
                if (chatRoom.getPharmacist() != null && chatRoom.getPharmacist().getId().equals(senderId)) {
                    sender = chatRoom.getPharmacist();
                } else {
                    sender = pharmacistUserRepository.findById(senderId).orElse(null);
                }
            } else { // ADMIN (pharmacy admin)
                sender = pharmacyAdminRepository.findById(senderId).orElse(null);
            }
            if (sender == null) {
                throw new IllegalArgumentException("invalid pharmacy sender");
            }
            isCustomerSender = false;
        } else {
            throw new IllegalArgumentException("invalid sender");
        }

        Message message = Message.builder()
                .chatRoom(chatRoom)
                .sender(sender != null ? sender : chatRoom.getCustomer())
                .content(text)
                .isRead(false)
                .timestamp(java.time.LocalDateTime.now())
                .build();
        messageRepository.save(message);

        chatRoom.updateLastMessageTime();
        chatRoom.incrementUnreadCount(isCustomerSender);
        chatRoomRepository.save(chatRoom);

        // Build payload to match WS consumer
        java.util.Map<String, Object> payload = java.util.Map.of(
                "customerId", chatRoom.getCustomer().getId(),
                "sender", isCustomerSender ? "customer" : "admin",
                "text", text,
                "time", System.currentTimeMillis()
        );

        // Deliver to the customer user destination
        String customerUserName = "customer:" + chatRoom.getCustomer().getId();
        messagingTemplate.convertAndSendToUser(customerUserName, "/queue/chat", payload);

        // Deliver to watching staff (admin, pharmacy_admin, pharmacist)
        for (var w : watchRegistry.getWatchers(chatRoom.getCustomer().getId())) {
            String role = w.role();
            if ("admin".equalsIgnoreCase(role) || "pharmacy_admin".equalsIgnoreCase(role) || "pharmacist".equalsIgnoreCase(role)) {
                String target = role.toLowerCase() + ":" + w.userId();
                messagingTemplate.convertAndSendToUser(target, "/queue/chat", payload);
            }
        }

        // Also echo to pharmacy side primary destination if pharmacist/admin is logged as user destination
        Long pharmacySideUserId = chatRoom.getPharmacist() != null ? chatRoom.getPharmacist().getId() : null;
        if (pharmacySideUserId != null) {
            messagingTemplate.convertAndSendToUser("pharmacist:" + pharmacySideUserId, "/queue/chat", payload);
        }
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
        String senderType = sender instanceof Customer ? "CUSTOMER" : "PHARMACIST";

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
