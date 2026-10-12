package com.skillswap.controller;

import com.skillswap.dto.RequestCreateRequest;
import com.skillswap.dto.ExchangeRequestResponse;
import com.skillswap.model.ExchangeRequest;
import com.skillswap.model.Skill;
import com.skillswap.model.User;
import com.skillswap.repository.ExchangeRequestRepository;
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
public class ExchangeRequestController {

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final UserService userService;
    private final SkillRepository skillRepository;

    @GetMapping("/requests")
    public ResponseEntity<List<ExchangeRequestResponse>> getRequests() {
        User currentUser = currentUser();
        return ResponseEntity.ok(exchangeRequestRepository.findBySenderOrReceiverOrderByCreatedAtDesc(currentUser, currentUser)
                .stream()
                .map(ExchangeRequestResponse::from)
                .toList());
    }

    @PostMapping("/requests")
    public ResponseEntity<ExchangeRequestResponse> createRequest(@RequestBody RequestCreateRequest request) {
        User current = currentUser();
        if (!current.isActive()) {
            throw new IllegalArgumentException("Reactivate your profile before sending exchange requests.");
        }
        User receiver = userService.findById(request.receiverId());
        if (!receiver.isActive()) {
            throw new IllegalArgumentException("This profile is not currently accepting exchange requests.");
        }
        Skill wantedSkill = skillRepository.findByName(request.wantedSkill())
                .orElseGet(() -> skillRepository.save(new Skill(request.wantedSkill())));
        Skill offeredSkill = skillRepository.findByName(request.offeredSkill())
                .orElseGet(() -> skillRepository.save(new Skill(request.offeredSkill())));

        ExchangeRequest exchangeRequest = ExchangeRequest.builder()
                .sender(current)
                .receiver(receiver)
                .wantedSkill(wantedSkill)
                .offeredSkill(offeredSkill)
                .message(request.message())
                .status(ExchangeRequest.RequestStatus.PENDING)
                .build();

        return ResponseEntity.ok(ExchangeRequestResponse.from(exchangeRequestRepository.save(exchangeRequest)));
    }

    @PatchMapping("/requests/{id}/respond")
    public ResponseEntity<ExchangeRequestResponse> respond(@PathVariable Long id, @RequestParam String action) {
        ExchangeRequest request = exchangeRequestRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (!request.getReceiver().getEmail().equalsIgnoreCase(currentUser().getEmail())) {
            throw new IllegalArgumentException("Only the receiver can respond to a request.");
        }

        switch (action.toLowerCase()) {
            case "accept" -> request.setStatus(ExchangeRequest.RequestStatus.ACCEPTED);
            case "reject" -> request.setStatus(ExchangeRequest.RequestStatus.REJECTED);
            case "cancel" -> request.setStatus(ExchangeRequest.RequestStatus.CANCELLED);
            default -> throw new IllegalArgumentException("Unsupported action");
        }

        return ResponseEntity.ok(ExchangeRequestResponse.from(exchangeRequestRepository.save(request)));
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userService.getCurrentUser(auth.getName());
    }
}
