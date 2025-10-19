package com.leo.pillpathbackend.service;

import com.leo.pillpathbackend.dto.ChatMessageHistoryResponse;
import com.leo.pillpathbackend.dto.ChatRoomDTO;
import com.leo.pillpathbackend.dto.StartChatRequest;

import java.util.List;

public interface ChatService {
    ChatRoomDTO startChat(Long customerId, StartChatRequest request);
    List<ChatRoomDTO> getMyChats(Long userId, String userType);
    ChatRoomDTO getChatRoomById(Long chatRoomId);
    ChatMessageHistoryResponse getChatMessages(Long chatRoomId, int page, int size);
    int getUnreadCount(Long userId, String userType);
    void persistAndBroadcastMessage(Long chatRoomId, Long senderId, String userType, String text);
    void markChatAsRead(Long chatRoomId, Long userId, String userType);
}
