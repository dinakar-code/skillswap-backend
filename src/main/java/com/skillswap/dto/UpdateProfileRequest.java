package com.skillswap.dto;

public record UpdateProfileRequest(String name, String bio, String college, String course,
                                  Integer yearOfStudy, String interests,
                                  String availability, String learningPreference) {
}
