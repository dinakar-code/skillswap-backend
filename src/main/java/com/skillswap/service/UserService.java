package com.skillswap.service;

import com.skillswap.dto.AuthResponse;
import com.skillswap.dto.RegisterRequest;
import com.skillswap.dto.UpdateProfileRequest;
import com.skillswap.dto.UserResponse;
import com.skillswap.model.Skill;
import com.skillswap.model.User;
import com.skillswap.model.ExchangeRequest;
import com.skillswap.model.LearningSession;
import com.skillswap.model.Review;
import com.skillswap.repository.ChatMessageRepository;
import com.skillswap.repository.ExchangeRequestRepository;
import com.skillswap.repository.LearningSessionRepository;
import com.skillswap.repository.ReviewRepository;
import com.skillswap.repository.SkillRepository;
import com.skillswap.repository.UserRepository;
import com.skillswap.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final LearningSessionRepository learningSessionRepository;
    private final ReviewRepository reviewRepository;
    private final ChatMessageRepository chatMessageRepository;

    public User register(RegisterRequest request) {
        String email = request.email().trim();
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setTeachingSkills(resolveSkills(request.teachingSkills()));
        user.setLearningSkills(resolveSkills(request.learningSkills()));
        return userRepository.save(user);
    }

    public AuthResponse login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = findByEmail(email);
        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, toUserResponse(user));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public User getCurrentUser(String email) {
        return findByEmail(email);
    }

    public User deactivateAccount(String email) {
        User user = getCurrentUser(email);
        user.setActive(false);
        return userRepository.save(user);
    }

    public User reactivateAccount(String email) {
        User user = getCurrentUser(email);
        user.setActive(true);
        return userRepository.save(user);
    }

    @Transactional
    public void permanentlyDeleteAccount(String email) {
        User user = getCurrentUser(email);
        List<ExchangeRequest> requests =
                exchangeRequestRepository.findBySenderOrReceiverOrderByCreatedAtDesc(user, user);
        if (!requests.isEmpty()) {
            chatMessageRepository.deleteByExchangeRequestIn(requests);
            exchangeRequestRepository.deleteAll(requests);
        }

        List<LearningSession> sessions =
                learningSessionRepository.findByInitiatorOrPartnerOrderByScheduledAtDesc(user, user);
        learningSessionRepository.deleteAll(sessions);

        List<Review> reviews = reviewRepository.findByReviewerOrReviewee(user, user);
        reviewRepository.deleteAll(reviews);

        userRepository.delete(user);
    }

    public User updateProfile(String email, UpdateProfileRequest request) {
        User user = getCurrentUser(email);
        if (request.name() != null) user.setName(request.name());
        if (request.bio() != null) user.setBio(request.bio());
        if (request.college() != null) user.setCollege(request.college());
        if (request.course() != null) user.setCourse(request.course());
        if (request.yearOfStudy() != null) user.setYearOfStudy(request.yearOfStudy());
        if (request.interests() != null) user.setInterests(request.interests());
        if (request.availability() != null) user.setAvailability(request.availability());
        if (request.learningPreference() != null) user.setLearningPreference(request.learningPreference());
        return userRepository.save(user);
    }

    public User addTeachingSkill(String email, String skillName) {
        User user = getCurrentUser(email);
        Skill skill = findOrCreateSkill(skillName);
        user.getTeachingSkills().add(skill);
        return userRepository.save(user);
    }

    public User removeTeachingSkill(String email, String skillName) {
        User user = getCurrentUser(email);
        user.getTeachingSkills().removeIf(skill -> skill.getName().equalsIgnoreCase(skillName));
        return userRepository.save(user);
    }

    public User addLearningSkill(String email, String skillName) {
        User user = getCurrentUser(email);
        Skill skill = findOrCreateSkill(skillName);
        user.getLearningSkills().add(skill);
        return userRepository.save(user);
    }

    public User removeLearningSkill(String email, String skillName) {
        User user = getCurrentUser(email);
        user.getLearningSkills().removeIf(skill -> skill.getName().equalsIgnoreCase(skillName));
        return userRepository.save(user);
    }

    public Set<User> discoverUsers(String query, String currentEmail) {
        String normalized = query == null ? "" : query.trim();
        Set<User> result = userRepository.findByNameContainingIgnoreCase(normalized).stream()
                .filter(user -> !user.getEmail().equalsIgnoreCase(currentEmail))
                .collect(Collectors.toSet());

        if (normalized.isBlank()) {
            result.addAll(userRepository.findAll().stream()
                    .filter(user -> !user.getEmail().equalsIgnoreCase(currentEmail))
                    .collect(Collectors.toSet()));
        }

        return result.stream()
                .filter(User::isActive)
                .sorted(Comparator.comparingInt(User::getProfileCompletion).reversed())
                .limit(20)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    public UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getBio(),
                user.getCollege(),
                user.getCourse(),
                user.getYearOfStudy(),
                user.getInterests(),
                user.getAvailability(),
                user.getLearningPreference(),
                user.isActive(),
                user.getProfileCompletion(),
                user.getTeachingSkills().stream().map(Skill::getName).sorted().toArray(String[]::new),
                user.getLearningSkills().stream().map(Skill::getName).sorted().toArray(String[]::new)
        );
    }

    private Set<Skill> resolveSkills(List<String> skillNames) {
        if (skillNames == null) {
            return Set.of();
        }

        return skillNames.stream()
                .filter(skillName -> skillName != null && !skillName.isBlank())
                .map(this::findOrCreateSkill)
                .collect(Collectors.toSet());
    }

    private Skill findOrCreateSkill(String skillName) {
        String normalizedName = skillName.trim();
        return skillRepository.findByName(normalizedName)
                .orElseGet(() -> skillRepository.save(new Skill(normalizedName)));
    }
}
