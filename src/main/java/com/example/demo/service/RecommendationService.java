package com.example.demo.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.User.UserRecommendationResponse;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.Follow_Unfollow.FollowUnfollowRepository;

@Service
@Transactional
public class RecommendationService {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowUnfollowRepository followRepository;

    /**
     * Get recommended users based on current user's role
     * Excludes users that the current user is already following
     */
    @Transactional(readOnly = true)
    public List<UserRecommendationResponse> getRecommendedUsers(Long currentUserId, int limit) {
        logger.info("Getting recommended users for user: {}", currentUserId);

        try {
            // Get current user
            Optional<User> currentUserOpt = userRepository.findById(currentUserId);
            if (currentUserOpt.isEmpty()) {
                logger.error("User not found with ID: {}", currentUserId);
                return List.of();
            }

            User currentUser = currentUserOpt.get();
            String userRole = currentUser.getRole();

            if (userRole == null) {
                userRole = "businessman"; // default role
            }

            final String finalUserRole = userRole; // Make it final for lambda usage
            logger.info("Current user role: {}", finalUserRole);

            // Get recommended roles based on current user's role
            List<String> recommendedRoles = getRecommendedRoles(finalUserRole.toLowerCase());

            // Get users with recommended roles (excluding current user)
            List<User> candidateUsers = userRepository.findByRoleInAndIdNot(recommendedRoles, currentUserId);

            // Get list of users that current user is already following
            Set<Long> followingUserIds = followRepository.findByFollowerId(currentUserId)
                    .stream()
                    .map(follow -> follow.getFollowee().getId())
                    .collect(Collectors.toSet());

            // Filter out users that are already being followed
            List<User> recommendedUsers = candidateUsers.stream()
                    .filter(user -> !followingUserIds.contains(user.getId()))
                    .limit(limit)
                    .collect(Collectors.toList());

            // Initialize lazy fields before DTO conversion
            recommendedUsers.forEach(user -> {
                if (user.getIndustry() != null) Hibernate.initialize(user.getIndustry());
                if (user.getInterests() != null) Hibernate.initialize(user.getInterests());
                if (user.getAchievements() != null) Hibernate.initialize(user.getAchievements());
            });

            // Convert to response DTOs
            return recommendedUsers.stream()
                    .map(user -> convertToRecommendationResponse(user, finalUserRole))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error getting recommended users for user {}: {}", currentUserId, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get recommended roles based on user's current role
     */
    private List<String> getRecommendedRoles(String userRole) {
        Map<String, List<String>> roleRecommendations = new HashMap<>();

        roleRecommendations.put("businessman", Arrays.asList("businessman", "investor", "entrepreneur"));
        roleRecommendations.put("investor", Arrays.asList("businessman", "investor", "entrepreneur"));
        roleRecommendations.put("entrepreneur",
                Arrays.asList("businessman", "investor", "entrepreneur", "innovator", "student", "researcher"));
        roleRecommendations.put("innovator", Arrays.asList("entrepreneur", "innovator", "researcher"));
        roleRecommendations.put("student", Arrays.asList("entrepreneur", "university-teacher", "researcher"));
        roleRecommendations.put("policy-maker",
                Arrays.asList("businessman", "investor", "entrepreneur", "innovator", "government-official"));
        roleRecommendations.put("government-official",
                Arrays.asList("businessman", "investor", "entrepreneur", "innovator", "policy-maker"));
        roleRecommendations.put("university-teacher",
                Arrays.asList("investor", "entrepreneur", "student", "university-teacher", "researcher"));
        roleRecommendations.put("researcher",
                Arrays.asList("businessman", "investor", "entrepreneur", "innovator", "researcher"));

        return roleRecommendations.getOrDefault(userRole, Arrays.asList("businessman", "investor", "entrepreneur"));
    }

    /**
     * Convert User entity to UserRecommendationResponse
     */
    private UserRecommendationResponse convertToRecommendationResponse(User user, String currentUserRole) {
        UserRecommendationResponse response = new UserRecommendationResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setProfilePictureUrl(user.getProfilePictureUrl());
        response.setIndustry(user.getIndustry());
        response.setInterests(user.getInterests());
        response.setAchievements(user.getAchievements());

        String reason = generateRecommendationReason(currentUserRole, user.getRole());
        response.setReasonForRecommendation(reason);

        return response;
    }

    /**
     * Generate a human-readable reason for recommendation
     */
    private String generateRecommendationReason(String currentUserRole, String recommendedUserRole) {
        if (currentUserRole == null || recommendedUserRole == null) {
            return "Based on your professional interests";
        }

        String current = currentUserRole.toLowerCase();
        String recommended = recommendedUserRole.toLowerCase();

        if (current.equals(recommended)) {
            return "Same professional background as you";
        }

        Map<String, Map<String, String>> reasonMap = new HashMap<>();

        Map<String, String> businessmanReasons = new HashMap<>();
        businessmanReasons.put("investor", "Great for funding and investment opportunities");
        businessmanReasons.put("entrepreneur", "Ideal for business partnerships and collaborations");
        reasonMap.put("businessman", businessmanReasons);

        Map<String, String> investorReasons = new HashMap<>();
        investorReasons.put("businessman", "Potential business opportunities to invest in");
        investorReasons.put("entrepreneur", "Innovative startups and investment prospects");
        reasonMap.put("investor", investorReasons);

        Map<String, String> entrepreneurReasons = new HashMap<>();
        entrepreneurReasons.put("businessman", "Business expertise and market insights");
        entrepreneurReasons.put("investor", "Funding and financial guidance opportunities");
        entrepreneurReasons.put("innovator", "Technology and innovation collaboration");
        entrepreneurReasons.put("student", "Fresh perspectives and potential talent");
        entrepreneurReasons.put("researcher", "Research insights and academic collaboration");
        reasonMap.put("entrepreneur", entrepreneurReasons);

        Map<String, String> studentReasons = new HashMap<>();
        studentReasons.put("entrepreneur", "Learn from startup experience and mentorship");
        studentReasons.put("university-teacher", "Academic guidance and educational support");
        studentReasons.put("researcher", "Research opportunities and academic collaboration");
        reasonMap.put("student", studentReasons);

        return reasonMap.getOrDefault(current, new HashMap<>())
                .getOrDefault(recommended, "Based on complementary professional interests");
    }
}
