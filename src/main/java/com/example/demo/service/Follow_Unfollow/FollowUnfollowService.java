package com.example.demo.service.Follow_Unfollow;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.User.UserResponse;
import com.example.demo.model.User;
import com.example.demo.model.Follow_Unfollow.Follow;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.Follow_Unfollow.FollowUnfollowRepository;

@Service
public class FollowUnfollowService {

    // This service will handle the business logic for following and unfollowing
    // users.
    // It will interact with the FollowUnfollowRepository to perform database
    // operations.

    // Example methods could include:
    // - followUser(Long followerId, Long followeeId)
    // - unfollowUser(Long followerId, Long followeeId)
    // - getFollowers(Long userId)
    // - getFollowing(Long userId)

    // Implement these methods as needed, ensuring to handle exceptions and
    // validations.

    @Autowired
    private FollowUnfollowRepository followUnfollowRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    /**
     * Makes the user with ID = followerId follow the user with ID = followeeId.
     *
     * @param followerId the ID of the user who wants to follow
     * @param followeeId the ID of the user to be followed
     * @throws IllegalArgumentException if followerId == followeeId,
     *                                  if either user does not exist,
     *                                  or if the follow relationship already exists
     */
    @Transactional
    public void followUser(Long followerId, Long followeeId) {
        // 1) Prevent a user from following themselves
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("You cannot follow yourself.");
        }

        // 2) Load the follower and followee User entities (throw if missing)
        User follower = userRepo.findById(followerId)
                .orElseThrow(() -> new IllegalArgumentException("Follower user not found."));
        User followee = userRepo.findById(followeeId)
                .orElseThrow(() -> new IllegalArgumentException("User to follow not found."));

        // 3) Check if a follow relationship already exists
        boolean alreadyFollows = followUnfollowRepo.existsByFollowerIdAndFolloweeId(followerId, followeeId);
        if (alreadyFollows) {
            throw new IllegalArgumentException("You are already following this user.");
        }

        // 4) Create and save the new Follow entity
        Follow newFollow = new Follow(follower, followee);
        followUnfollowRepo.save(newFollow);

        //create interaction key in Redis
        String interactionKey = "interaction:" + followerId + "," + followeeId;
        if(interactionKey!=null && !interactionKey.isEmpty()) {
            redisTemplate.opsForValue().set(interactionKey, "1");
            System.out.println("Interaction key "+ interactionKey + " created with value 1");
        }
    }

    @Transactional
    public void unfollowUser(Long followerId, Long followeeId) {
        // 1) Prevent a user from unfollowing themselves
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("You cannot unfollow yourself.");
        }
        
        System.out.println("Unfollow user with ID: " + followeeId + " by follower ID: " + followerId);
        // 2) Check if the follow relationship exists
        boolean exists = followUnfollowRepo.existsByFollowerIdAndFolloweeId(followerId, followeeId);
        if (!exists) {
            throw new IllegalArgumentException("You are not following this user.");
        }

        // 3) Remove the follow relationship
        followUnfollowRepo.deleteByFollowerIdAndFolloweeId(followerId, followeeId);

        // 4) Reset interaction count in Redis
        String interactionKey = "interaction:" + followerId + "," + followeeId;
        if(interactionKey!=null && !interactionKey.isEmpty())
            redisTemplate.delete(interactionKey);
        System.out.println("Interaction key "+ interactionKey);    
    }

    /**
     * Returns a list of UserResponse DTOs for all users that `userId` is following.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllFollowees(Long userId) {
        // 1) Verify the user exists (throws if not)
        userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2) Fetch all Follow entities where follower.id == userId
        List<Follow> followEntries = followUnfollowRepo.findByFollowerId(userId);

        // 3) Map each Follow → its followee User → UserResponse DTO
        return followEntries.stream()
                .map(follow -> {
                    User followee = follow.getFollowee();
                    UserResponse dto = new UserResponse();
                    dto.setId(followee.getId());
                    dto.setUsername(followee.getUsername());
                    dto.setProfilePictureUrl(followee.getProfilePictureUrl());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns a list of UserResponse DTOs for all users who follow `userId`.
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllFollowers(Long userId) {
        // 1) Verify the user exists (throws if not)
        userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 2) Fetch all Follow entities where followee.id == userId
        List<Follow> followEntries = followUnfollowRepo.findByFolloweeId(userId);

        // 3) Map each Follow → its follower User → UserResponse DTO
        return followEntries.stream()
                .map(follow -> {
                    User follower = follow.getFollower();
                    UserResponse dto = new UserResponse();
                    dto.setId(follower.getId());
                    dto.setUsername(follower.getUsername());
                    dto.setProfilePictureUrl(follower.getProfilePictureUrl());
                    return dto;
                })
                .collect(Collectors.toList());
    }

}
