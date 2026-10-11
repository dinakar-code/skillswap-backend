package com.skillswap.repository;

import com.skillswap.model.LearningSession;
import com.skillswap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LearningSessionRepository extends JpaRepository<LearningSession, Long> {
    List<LearningSession> findByInitiatorOrPartnerOrderByScheduledAtDesc(User initiator, User partner);
    List<LearningSession> findByInitiatorOrderByScheduledAtDesc(User initiator);
    List<LearningSession> findByPartnerOrderByScheduledAtDesc(User partner);
}
