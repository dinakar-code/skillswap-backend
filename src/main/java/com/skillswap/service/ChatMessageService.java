package com.skillswap.service;

import com.skillswap.dto.ChatMessageRequest;
import com.skillswap.dto.ChatMessageResponse;
import com.skillswap.model.ChatMessage;
import com.skillswap.model.ExchangeRequest;
import com.skillswap.model.User;
import com.skillswap.repository.ChatMessageRepository;
import com.skillswap.repository.ExchangeRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessages(Long requestId, String email) {
        ExchangeRequest request = getAuthorizedAcceptedRequest(requestId, email);
        return chatMessageRepository.findByExchangeRequestOrderByCreatedAtAsc(request).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long requestId, String email, ChatMessageRequest messageRequest) {
        User sender = userService.getCurrentUser(email);
        if (!sender.isActive()) {
            throw new IllegalArgumentException("Reactivate your profile before sending messages.");
        }
        ExchangeRequest request = getAuthorizedAcceptedRequest(requestId, email);
        String content = messageRequest.content() == null ? "" : messageRequest.content().trim();
        if (content.isEmpty() || content.length() > 2000) {
            throw new IllegalArgumentException("Message must contain between 1 and 2000 characters.");
        }

        ChatMessage message = ChatMessage.builder()
                .exchangeRequest(request)
                .sender(sender)
                .content(content)
                .build();
        return toResponse(chatMessageRepository.save(message));
    }

    private ExchangeRequest getAuthorizedAcceptedRequest(Long requestId, String email) {
        ExchangeRequest request = exchangeRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Exchange request not found."));
        String normalizedEmail = email.toLowerCase();
        boolean participant = request.getSender().getEmail().equalsIgnoreCase(normalizedEmail)
                || request.getReceiver().getEmail().equalsIgnoreCase(normalizedEmail);
        if (!participant) {
            throw new IllegalArgumentException("You are not a participant in this exchange.");
        }
        if (request.getStatus() != ExchangeRequest.RequestStatus.ACCEPTED) {
            throw new IllegalArgumentException("Chat is available for accepted exchanges only.");
        }
        return request;
    }

    private ChatMessageResponse toResponse(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getName(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
