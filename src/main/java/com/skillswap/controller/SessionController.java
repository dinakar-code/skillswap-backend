package com.skillswap.controller;

import com.skillswap.dto.SessionRequest;
import com.skillswap.model.LearningSession;
import com.skillswap.model.Skill;
import com.skillswap.model.User;
import com.skillswap.repository.LearningSessionRepository;
import com.skillswap.repository.SkillRepository;
import com.skillswap.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SessionController {

    private final LearningSessionRepository sessionRepository;
    private final SkillRepository skillRepository;
    private final UserService userService;

    @GetMapping("/sessions")
    public ResponseEntity<List<LearningSession>> getSessions() {
        User current = currentUser();
        return ResponseEntity.ok(sessionRepository.findByInitiatorOrPartnerOrderByScheduledAtDesc(current, current));
    }

    @PostMapping("/sessions")
    public ResponseEntity<LearningSession> createSession(@RequestBody SessionRequest request) {
        User current = currentUser();
        User partner = userService.findById(request.partnerId());
        Skill skill = skillRepository.findByName(request.skillName())
                .orElseGet(() -> skillRepository.save(new Skill(request.skillName())));

        LearningSession session = LearningSession.builder()
                .initiator(current)
                .partner(partner)
                .skill(skill)
                .scheduledAt(request.scheduledAt())
                .durationMinutes(request.durationMinutes())
                .objectives(request.objectives())
                .status(LearningSession.SessionStatus.SCHEDULED)
                .build();

        return ResponseEntity.ok(sessionRepository.save(session));
    }

    @PatchMapping("/sessions/{id}/complete")
    public ResponseEntity<LearningSession> completeSession(@PathVariable Long id) {
        LearningSession session = sessionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Session not found"));

        if (!(session.getInitiator().getEmail().equalsIgnoreCase(currentUser().getEmail()) || session.getPartner().getEmail().equalsIgnoreCase(currentUser().getEmail()))) {
            throw new IllegalArgumentException("You are not allowed to update this session");
        }

        session.setStatus(LearningSession.SessionStatus.COMPLETED);
        return ResponseEntity.ok(sessionRepository.save(session));
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.getCurrentUser(auth.getName());
    }
}
