package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.User.UserRecommendationResponse;
import com.example.demo.service.RecommendationService;

@RestController
@RequestMapping("/api/v1/recommended")
public class RecommendationController {

    private static final Logger logger = LoggerFactory.getLogger(RecommendationController.class);

    @Autowired
    private RecommendationService recommendationService;

    /**
     * GET /api/v1/recommended/users
     * Get recommended users for the authenticated user based on their role
     */
    @GetMapping("/users")
    public ResponseEntity<?> getRecommendedUsers(
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            Authentication authentication) {

        try {
            // Get current user ID from JWT token
            Long currentUserId = Long.valueOf(authentication.getName());
            logger.info("Getting recommended users for user: {} with limit: {}", currentUserId, limit);

            // Validate limit parameter
            if (limit <= 0 || limit > 50) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Limit must be between 1 and 50"));
            }

            // Get recommended users
            List<UserRecommendationResponse> recommendedUsers = recommendationService.getRecommendedUsers(currentUserId,
                    limit);

            logger.info("Found {} recommended users for user: {}", recommendedUsers.size(), currentUserId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", recommendedUsers,
                    "total", recommendedUsers.size(),
                    "message", "Recommended users retrieved successfully"));

        } catch (NumberFormatException e) {
            logger.error("Invalid user ID format in authentication: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid authentication token"));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid parameters: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error getting recommended users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve recommended users"));
        }
    }

    /**
     * GET /api/v1/recommended/users/count
     * Get count of available recommended users for the authenticated user
     */
    @GetMapping("/users/count")
    public ResponseEntity<?> getRecommendedUsersCount(Authentication authentication) {

        try {
            // Get current user ID from JWT token
            Long currentUserId = Long.valueOf(authentication.getName());
            logger.info("Getting recommended users count for user: {}", currentUserId);

            // Get recommended users with high limit to count all
            List<UserRecommendationResponse> recommendedUsers = recommendationService.getRecommendedUsers(currentUserId,
                    1000);

            int totalCount = recommendedUsers.size();
            logger.info("Total recommended users count for user {}: {}", currentUserId, totalCount);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "totalRecommendations", totalCount,
                    "message", "Recommended users count retrieved successfully"));

        } catch (NumberFormatException e) {
            logger.error("Invalid user ID format in authentication: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid authentication token"));
        } catch (Exception e) {
            logger.error("Error getting recommended users count: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve recommended users count"));
        }
    }
}
