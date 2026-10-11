package com.skillswap.dto;

import java.time.LocalDateTime;

public record SessionRequest(Long partnerId, String skillName, LocalDateTime scheduledAt,
                            Integer durationMinutes, String objectives) {
}
