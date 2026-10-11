package com.skillswap.dto;

public record RequestCreateRequest(Long receiverId, String wantedSkill, String offeredSkill, String message) {
}
