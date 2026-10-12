package com.skillswap.dto;

import java.util.List;

public record RegisterRequest(
        String name,
        String email,
        String password,
        List<String> teachingSkills,
        List<String> learningSkills) {
}
