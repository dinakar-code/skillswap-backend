package com.skillswap.controller;

import com.skillswap.dto.ChatMessageRequest;
import com.skillswap.dto.ChatMessageResponse;
import com.skillswap.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests/{requestId}/messages")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @GetMapping
    public ResponseEntity<List<ChatMessageResponse>> getMessages(@PathVariable Long requestId) {
        return ResponseEntity.ok(chatMessageService.getMessages(requestId, currentEmail()));
    }

    @PostMapping
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @PathVariable Long requestId,
            @RequestBody ChatMessageRequest request) {
        return ResponseEntity.ok(chatMessageService.sendMessage(requestId, currentEmail(), request));
    }

    private String currentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
