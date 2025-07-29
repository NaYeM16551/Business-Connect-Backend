package com.example.demo.controller;


import java.util.List;
import java.util.Map;

import com.example.demo.dto.User.UserResponse;
import com.example.demo.service.Follow_Unfollow.FollowUnfollowService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/connections")
public class FollowUnfollowController {

    private final FollowUnfollowService followUnfollowService;

    @Autowired
    public FollowUnfollowController(FollowUnfollowService followUnfollowService) {
        this.followUnfollowService = followUnfollowService;
    }

    /**
     * POST /api/v1/connections/follow/{userID}
     * Authenticated user follows {userID}.
     */
    @PostMapping("/follow/{userID}")
    public ResponseEntity<?> followUser(
            @PathVariable("userID") Long followeeId,
            Authentication authentication ) {
        // With Spring Security, principal is guaranteed non-null (unless endpoint is not secured).
        Long followerId = Long.valueOf(authentication.getName());
        System.out.println("Following user: " + followeeId + " by follower: " + followerId);
        followUnfollowService.followUser(followerId, followeeId);
        return ResponseEntity.ok(Map.of("message", "Successfully followed user " + followeeId));
    }

    /**
     * DELETE /api/v1/connections/unfollow/{userID}
     * Authenticated user unfollows {userID}.
     */
    @DeleteMapping("/unfollow/{userID}")
    public ResponseEntity<?> unfollowUser(
            @PathVariable("userID") Long followeeId,
            Authentication authentication) {
        Long followerId = Long.valueOf(authentication.getName());
        System.out.println("Unfollowing user: " + followeeId + " by follower: " + followerId);
        followUnfollowService.unfollowUser(followerId, followeeId);
        return ResponseEntity.ok(Map.of("message", "Successfully unfollowed user " + followeeId));
    }

    /**
     * GET /api/v1/connections/following/{userID}
     * Returns the list of User objects that {userID} is following.
     */
    @GetMapping("/following")
    public ResponseEntity<?> getFollowing(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        System.out.println("Getting following list for user: " + userId);
        List<UserResponse> followingList = followUnfollowService.getAllFollowees(userId);
        System.out.println("Following list size: " + followingList.size());
        return ResponseEntity.ok(followingList);
    }

    /**
     * GET /api/v1/connections/followers/{userID}
     * Returns the list of User objects who follow {userID}.
     */
    @GetMapping("/followers/{userID}")
    public ResponseEntity<?> getFollowers(@PathVariable("userID") Long userId, Authentication authentication) {  
        List<UserResponse> followersList = followUnfollowService.getAllFollowers(userId);
        return ResponseEntity.ok(followersList);
    }

}
