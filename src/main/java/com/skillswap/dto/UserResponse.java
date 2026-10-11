package com.skillswap.dto;

public record UserResponse(Long id, String name, String email, String bio, String college,
                          String course, Integer yearOfStudy, String interests,
                          String availability, String learningPreference,
                          int profileCompletion, String[] teachingSkills, String[] learningSkills) {
}
