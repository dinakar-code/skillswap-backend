package com.skillswap.dto;

import java.time.LocalDateTime;

public record ChatMessageResponse(Long id, Long senderId, String senderName,
                                  String content, LocalDateTime createdAt) {
}
