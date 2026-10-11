package com.skillswap.controller;

import com.skillswap.dto.SkillRequest;
import com.skillswap.dto.UpdateProfileRequest;
import com.skillswap.dto.UserResponse;
import com.skillswap.model.User;
import com.skillswap.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/users/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        String email = currentEmail();
        return ResponseEntity.ok(userService.toUserResponse(userService.getCurrentUser(email)));
    }

    @PutMapping("/users/me")
    public ResponseEntity<UserResponse> updateCurrentUser(@RequestBody UpdateProfileRequest request) {
        String email = currentEmail();
        return ResponseEntity.ok(userService.toUserResponse(userService.updateProfile(email, request)));
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam(required = false, defaultValue = "") String query) {
        String email = currentEmail();
        Set<User> users = userService.discoverUsers(query, email);
        return ResponseEntity.ok(users.stream().map(userService::toUserResponse).collect(Collectors.toList()));
    }

    @PostMapping("/users/skills/teach")
    public ResponseEntity<UserResponse> addTeachingSkill(@RequestBody SkillRequest request) {
        return ResponseEntity.ok(userService.toUserResponse(userService.addTeachingSkill(currentEmail(), request.skillName())));
    }

    @DeleteMapping("/users/skills/teach/{skillName}")
    public ResponseEntity<UserResponse> removeTeachingSkill(@PathVariable String skillName) {
        return ResponseEntity.ok(userService.toUserResponse(userService.removeTeachingSkill(currentEmail(), skillName)));
    }

    @PostMapping("/users/skills/learn")
    public ResponseEntity<UserResponse> addLearningSkill(@RequestBody SkillRequest request) {
        return ResponseEntity.ok(userService.toUserResponse(userService.addLearningSkill(currentEmail(), request.skillName())));
    }

    @DeleteMapping("/users/skills/learn/{skillName}")
    public ResponseEntity<UserResponse> removeLearningSkill(@PathVariable String skillName) {
        return ResponseEntity.ok(userService.toUserResponse(userService.removeLearningSkill(currentEmail(), skillName)));
    }

    private String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getName();
    }
}
