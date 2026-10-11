package com.skillswap.controller;

import com.skillswap.model.User;
import com.skillswap.service.DashboardService;
import com.skillswap.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final UserService userService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getCurrentUser(auth.getName());
        return ResponseEntity.ok(dashboardService.buildDashboard(user));
    }
}
