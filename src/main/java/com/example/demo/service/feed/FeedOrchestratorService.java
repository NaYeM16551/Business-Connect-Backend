package com.example.demo.service.feed;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.example.demo.dto.Posts.PostCreatedEventDto;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.Follow_Unfollow.FollowUnfollowRepository;

@Service
public class FeedOrchestratorService {

    private final FollowUnfollowRepository followRepo; // JPA repo for “Follow” entity
    private final StringRedisTemplate redis; // For ZADD, HMSET, etc.
    private final UserRepository userRepo; // To fetch author details

    @Autowired
    public FeedOrchestratorService(FollowUnfollowRepository followRepo,
            StringRedisTemplate redis,
            UserRepository userRepo) {
        this.followRepo = followRepo;
        this.redis = redis;
        this.userRepo = userRepo;
    }

    // Helper method to safely execute Redis operations
    private void safeRedisOperation(Runnable operation) {
        try {
            operation.run();
        } catch (Exception e) {
            System.err.println("❌ Redis operation failed in FeedOrchestratorService: " + e.getMessage());
            System.err.println("⚠️ Post creation will continue, but Redis storage may be incomplete");
        }
    }

    /**
     * Listens for any PostCreatedEvent (either a brand‐new post or a share) and
     * “fans out”:
     *
     * 1) Writes/updates the Redis Hash "post:{postId}" exactly once per event.
     * 2) Adds the postId into each follower’s ZSET “feed:user:{followerId}” with a
     * score = nowMs.
     * This way, even if the original creation was days ago, a share pushes it to
     * “now.”
     */
    @EventListener
    public void onPostCreated(PostCreatedEventDto event) {
        System.out.println("🎯 FeedOrchestratorService received PostCreatedEvent for post ID: " + event.getPostId());

        Long postId = event.getPostId();
        Long authorId = event.getAuthorId();
        if (postId == null || authorId == null) {
            System.err.println("❌ Invalid event: postId or authorId is null");
            return;
        }

        // 1) Fetch author details once
        User author = userRepo.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));
        String authorName = author.getUsername();
        String authorAvatar = author.getProfilePictureUrl();

        // 2) Build the single Redis Hash for this post (overwrite / upsert)
        String postKey = "post:" + postId;
        // authorId
        safeRedisOperation(() -> redis.opsForHash().put(postKey, "authorId", authorId.toString()));
        // authorName (store empty string if null)
        safeRedisOperation(() -> redis.opsForHash().put(postKey, "authorName", authorName == null ? "" : authorName));
        // authorAvatarUrl (store empty string if null)
        safeRedisOperation(
                () -> redis.opsForHash().put(postKey, "authorAvatarUrl", authorAvatar == null ? "" : authorAvatar));
        // snippet (content)
        String snippet = event.getContentSnippet();
        safeRedisOperation(() -> redis.opsForHash().put(postKey, "content", snippet == null ? "" : snippet));
        // createdAt: keep the original creation time for recency‐decay in the API
        Instant createdAt = event.getCreatedAt();
        String createdAtStr = (createdAt == null ? "" : createdAt.toString());
        safeRedisOperation(() -> redis.opsForHash().put(postKey, "createdAt", createdAtStr));
        // mediaUrls as CSV
        List<String> mediaUrls = event.getMediaUrls();
        final String csvMedia;
        if (mediaUrls != null && !mediaUrls.isEmpty()) {
            csvMedia = mediaUrls.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
        } else {
            csvMedia = "";
        }
        safeRedisOperation(() -> redis.opsForHash().put(postKey, "mediaUrls", csvMedia));
        // initialize counts to zero if missing (upsert)
        safeRedisOperation(() -> redis.opsForHash().putIfAbsent(postKey, "likeCount", "0"));
        safeRedisOperation(() -> redis.opsForHash().putIfAbsent(postKey, "commentCount", "0"));
        safeRedisOperation(() -> redis.opsForHash().putIfAbsent(postKey, "shareCount", "0"));
        // optional TTL
        safeRedisOperation(() -> redis.expire(postKey, Duration.ofDays(7)));

        // 3) Now fan out to each follower’s sorted set
        // Use post creation time as the score for new posts (recency/decay)
        long score = createdAt != null ? createdAt.toEpochMilli() : System.currentTimeMillis();
        List<Long> followerIds = followRepo.findFollowerIdsByFolloweeId(authorId);
        // If you want the author to see their own post too, uncomment the next line:
        followerIds.add(authorId);

        for (Long followerId : followerIds) {
            if (followerId == null)
                continue;
            String zsetKey = "feed:user:" + followerId;
            // ZADD feed:user:{followerId} {score} {postId}
            safeRedisOperation(() -> redis.opsForZSet().add(zsetKey, postId.toString(), score));
            System.out.println("✅ Added post " + postId + " to follower " + followerId + "'s feed at score " + score);
        }

        System.out.println("🎉 Successfully processed PostCreatedEvent for post ID: " + postId);
    }
}
