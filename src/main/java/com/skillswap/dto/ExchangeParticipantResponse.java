package com.skillswap.dto;

import com.skillswap.model.Skill;
import com.skillswap.model.User;

import java.util.Comparator;
import java.util.List;

public record ExchangeParticipantResponse(Long id, String name, String email,
                                          List<SkillSummary> teachingSkills,
                                          List<SkillSummary> learningSkills) {

    public static ExchangeParticipantResponse from(User user) {
        return new ExchangeParticipantResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getTeachingSkills().stream()
                        .sorted(Comparator.comparing(Skill::getName))
                        .map(skill -> new SkillSummary(skill.getId(), skill.getName()))
                        .toList(),
                user.getLearningSkills().stream()
                        .sorted(Comparator.comparing(Skill::getName))
                        .map(skill -> new SkillSummary(skill.getId(), skill.getName()))
                        .toList()
        );
    }
}
