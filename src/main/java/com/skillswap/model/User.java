package com.skillswap.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(length = 400)
    private String bio;

    private String college;
    private String course;
    private Integer yearOfStudy;
    private String interests;

    private String availability;
    private String learningPreference;

    @Column(nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private boolean active = true;

    @ManyToMany
    @JoinTable(
        name = "user_teaching_skills",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> teachingSkills = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "user_learning_skills",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> learningSkills = new HashSet<>();

    @Transient
    public int getProfileCompletion() {
        int score = 0;
        if (name != null && !name.isBlank()) score += 20;
        if (bio != null && !bio.isBlank()) score += 20;
        if (college != null && !college.isBlank()) score += 15;
        if (course != null && !course.isBlank()) score += 15;
        if (interests != null && !interests.isBlank()) score += 15;
        if (teachingSkills != null && !teachingSkills.isEmpty()) score += 15;
        return Math.min(score, 100);
    }
}
