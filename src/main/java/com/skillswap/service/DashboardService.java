package com.skillswap.service;

import com.skillswap.model.ExchangeRequest;
import com.skillswap.model.LearningSession;
import com.skillswap.model.Review;
import com.skillswap.model.User;
import com.skillswap.repository.ExchangeRequestRepository;
import com.skillswap.repository.LearningSessionRepository;
import com.skillswap.repository.ReviewRepository;
import com.skillswap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final LearningSessionRepository learningSessionRepository;
    private final ReviewRepository reviewRepository;
    private final MatchingService matchingService;

    public Map<String, Object> buildDashboard(User currentUser) {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("profile", currentUser);
        dashboard.put("teachingSkills", currentUser.getTeachingSkills());
        dashboard.put("learningSkills", currentUser.getLearningSkills());

        List<User> users = userRepository.findAll();
        List<Map<String, Object>> matches = matchingService.findMatches(currentUser, users);
        dashboard.put("suggestedPartners", matches);

        List<ExchangeRequest> incomingRequests = exchangeRequestRepository.findByReceiverOrderByCreatedAtDesc(currentUser);
        dashboard.put("incomingRequests", incomingRequests);

        List<ExchangeRequest> outgoingRequests = exchangeRequestRepository.findBySenderOrderByCreatedAtDesc(currentUser);
        dashboard.put("outgoingRequests", outgoingRequests);

        List<LearningSession> sessions = learningSessionRepository.findByInitiatorOrPartnerOrderByScheduledAtDesc(currentUser, currentUser);
        dashboard.put("upcomingSessions", sessions.stream().filter(s -> s.getStatus() != LearningSession.SessionStatus.COMPLETED && s.getStatus() != LearningSession.SessionStatus.CANCELLED).toList());
        dashboard.put("completedSessions", sessions.stream().filter(s -> s.getStatus() == LearningSession.SessionStatus.COMPLETED).toList());

        List<Review> reviews = reviewRepository.findByRevieweeOrderByCreatedAtDesc(currentUser);
        dashboard.put("reviews", reviews);

        return dashboard;
    }
}
