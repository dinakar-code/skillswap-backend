package com.skillswap.repository;

import com.skillswap.model.ChatMessage;
import com.skillswap.model.ExchangeRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByExchangeRequestOrderByCreatedAtAsc(ExchangeRequest exchangeRequest);
    void deleteByExchangeRequestIn(Iterable<ExchangeRequest> exchangeRequests);
}
