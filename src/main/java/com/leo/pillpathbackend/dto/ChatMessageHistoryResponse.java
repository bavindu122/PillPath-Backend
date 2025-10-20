package com.leo.pillpathbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageHistoryResponse {
	private Long chatRoomId;
	private List<MessageDTO> messages;
	private int currentPage;
	private int totalPages;
	private long totalMessages;
	private boolean hasMore;
}
