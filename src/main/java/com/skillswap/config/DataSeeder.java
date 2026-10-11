package com.skillswap.config;

import com.skillswap.model.Skill;
import com.skillswap.model.User;
import com.skillswap.repository.SkillRepository;
import com.skillswap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final SkillRepository skillRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (skillRepository.count() == 0) {
            skillRepository.saveAll(Set.of(
                    new Skill("Java"),
                    new Skill("Python"),
                    new Skill("React"),
                    new Skill("Spring Boot"),
                    new Skill("UI/UX Design"),
                    new Skill("Data Structures"),
                    new Skill("Machine Learning"),
                    new Skill("Public Speaking")
            ));
        }

        if (userRepository.count() == 0) {
            Skill javaSkill = skillRepository.findByName("Java").orElseThrow();
            Skill springSkill = skillRepository.findByName("Spring Boot").orElseThrow();
            Skill pythonSkill = skillRepository.findByName("Python").orElseThrow();
            Skill mlSkill = skillRepository.findByName("Machine Learning").orElseThrow();
            Skill dataStructuresSkill = skillRepository.findByName("Data Structures").orElseThrow();
            Skill reactSkill = skillRepository.findByName("React").orElseThrow();
            Skill uxSkill = skillRepository.findByName("UI/UX Design").orElseThrow();
            Skill speakingSkill = skillRepository.findByName("Public Speaking").orElseThrow();

            User alice = new User();
            alice.setName("Alice Johnson");
            alice.setEmail("alice@skillswap.dev");
            alice.setPassword(passwordEncoder.encode("password123"));
            alice.setBio("Third-year computer science student who loves mentoring junior devs.");
            alice.setCollege("Northbridge University");
            alice.setCourse("BSc Computer Science");
            alice.setYearOfStudy(3);
            alice.setInterests("Java, mentoring, backend systems");
            alice.setTeachingSkills(Set.of(javaSkill, springSkill));
            alice.setLearningSkills(Set.of(pythonSkill, mlSkill));

            User bob = new User();
            bob.setName("Bob Carter");
            bob.setEmail("bob@skillswap.dev");
            bob.setPassword(passwordEncoder.encode("password123"));
            bob.setBio("Python enthusiast and product-minded builder.");
            bob.setCollege("City Tech Campus");
            bob.setCourse("BSc Data Science");
            bob.setYearOfStudy(2);
            bob.setInterests("Python, analytics, teaching");
            bob.setTeachingSkills(Set.of(pythonSkill, dataStructuresSkill));
            bob.setLearningSkills(Set.of(javaSkill, reactSkill));

            User carol = new User();
            carol.setName("Carol Lee");
            carol.setEmail("carol@skillswap.dev");
            carol.setPassword(passwordEncoder.encode("password123"));
            carol.setBio("Design-focused developer exploring better UX outcomes.");
            carol.setCollege("Lakeside Institute");
            carol.setCourse("B.A. HCI");
            carol.setYearOfStudy(4);
            carol.setInterests("UX, accessibility, visual design");
            carol.setTeachingSkills(Set.of(uxSkill, reactSkill));
            carol.setLearningSkills(Set.of(speakingSkill, springSkill));

            userRepository.saveAll(Set.of(alice, bob, carol));
        }
    }
}
