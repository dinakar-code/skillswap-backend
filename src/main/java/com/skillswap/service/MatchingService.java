package com.skillswap.service;

import com.skillswap.model.Skill;
import com.skillswap.model.User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MatchingService {

    public List<Map<String, Object>> findMatches(User currentUser, List<User> allUsers) {
        List<Map<String, Object>> matches = new ArrayList<>();
        Set<Skill> currentLearning = currentUser.getLearningSkills();
        Set<Skill> currentTeaching = currentUser.getTeachingSkills();

        for (User other : allUsers) {
            if (other.getId().equals(currentUser.getId())) {
                continue;
            }

            boolean mutualTeachLearn = currentLearning.stream().anyMatch(skill -> other.getTeachingSkills().stream().anyMatch(s -> s.getName().equalsIgnoreCase(skill.getName())))
                    && other.getLearningSkills().stream().anyMatch(skill -> currentTeaching.stream().anyMatch(s -> s.getName().equalsIgnoreCase(skill.getName())));

            if (!mutualTeachLearn) {
                continue;
            }

            Skill learnedSkill = currentLearning.stream()
                    .filter(skill -> other.getTeachingSkills().stream().anyMatch(s -> s.getName().equalsIgnoreCase(skill.getName())))
                    .findFirst()
                    .orElse(null);

            Skill offeredSkill = other.getLearningSkills().stream()
                    .filter(skill -> currentTeaching.stream().anyMatch(s -> s.getName().equalsIgnoreCase(skill.getName())))
                    .findFirst()
                    .orElse(null);

            if (learnedSkill == null || offeredSkill == null) {
                continue;
            }

            Map<String, Object> match = new HashMap<>();
            match.put("userId", other.getId());
            match.put("name", other.getName());
            match.put("email", other.getEmail());
            match.put("matchReason", "You want to learn " + learnedSkill.getName() + " and can offer " + offeredSkill.getName() + " in return.");
            match.put("learningSkill", learnedSkill.getName());
            match.put("offeringSkill", offeredSkill.getName());
            matches.add(match);
        }

        return matches;
    }
}
