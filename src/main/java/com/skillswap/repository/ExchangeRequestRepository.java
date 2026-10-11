package com.skillswap.repository;

import com.skillswap.model.ExchangeRequest;
import com.skillswap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Long> {
    List<ExchangeRequest> findBySenderOrReceiverOrderByCreatedAtDesc(User sender, User receiver);
    List<ExchangeRequest> findByReceiverOrderByCreatedAtDesc(User receiver);
    List<ExchangeRequest> findBySenderOrderByCreatedAtDesc(User sender);
}
