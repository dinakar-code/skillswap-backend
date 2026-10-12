package com.skillswap.dto;

import com.skillswap.model.ExchangeRequest;

import java.time.LocalDateTime;

public record ExchangeRequestResponse(
        Long id,
        ExchangeParticipantResponse sender,
        ExchangeParticipantResponse receiver,
        SkillSummary wantedSkill,
        SkillSummary offeredSkill,
        String message,
        ExchangeRequest.RequestStatus status,
        LocalDateTime createdAt) {

    public static ExchangeRequestResponse from(ExchangeRequest request) {
        return new ExchangeRequestResponse(
                request.getId(),
                ExchangeParticipantResponse.from(request.getSender()),
                ExchangeParticipantResponse.from(request.getReceiver()),
                new SkillSummary(request.getWantedSkill().getId(), request.getWantedSkill().getName()),
                new SkillSummary(request.getOfferedSkill().getId(), request.getOfferedSkill().getName()),
                request.getMessage(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }
}
