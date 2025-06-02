package com.example.demo.service.feed;

import com.example.demo.dto.Posts.PostCreatedEventDto;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.Follow_Unfollow.FollowUnfollowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Objects;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class FeedOrchestratorService {

    private final FollowUnfollowRepository followRepo; // JPA repo for “Follow” entity
    private final StringRedisTemplate redis; // For ZADD, ZRANGE, HMSET, etc.
    private final UserRepository userRepo; // To fetch author details for denormalization

    @Autowired
    public FeedOrchestratorService(FollowUnfollowRepository followRepo,
            StringRedisTemplate redis,
            UserRepository userRepo) {
        this.followRepo = followRepo;
        this.redis = redis;
        this.userRepo = userRepo;
    }

    /**
     * Listens for any PostCreatedEvent and “fans out” to each follower:
     * 1) Adds the post ID into each follower’s ZSET (feed:user:{followerId}),
     * using the post’s timestamp‐based score.
     * 2) Stores a Redis Hash “post:{postId}” containing minimal post details
     * (authorName, snippet, mediaUrls, counts).
     *
     * If a single author has thousands of followers, this loop can be large.
     * In that case, you might use a “celebrity” fallback (not shown here).
     */
    @EventListener
    public void onPostCreated(PostCreatedEventDto event) {
        System.out.println("Received PostCreatedEvent: " + event);
        Long postId = event.getPostId();
        Long authorId = event.getAuthorId();
        Long score = event.getCreatedAt().toEpochMilli(); // numeric score for ZSET
        String snippet = event.getContentSnippet();
        List<String> mediaUrls = event.getMediaUrls();

        // 1) Fetch author details (e.g. username, avatarUrl) to include in the Redis
        // Hash
        User author = userRepo.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));
        String authorName = author.getUsername();
        String authorAvatar = author.getProfilePictureUrl(); // assume User has a getProfilePictureUrl()

        // 2) Get all follower IDs for this author from PostgreSQL
        // This runs a query like: SELECT f.follower.id FROM Follow f WHERE
        // f.followee.id = :authorId
        List<Long> followerIds = followRepo.findFollowerIdsByFolloweeId(authorId);

        // 3) For each follower, add the post ID into their personal feed sorted set
        //
        // ZADD feed:user:<followerId> <score> <postId>
        // We use the numeric score = epoch milliseconds so newer posts appear first in
        // ZREVRANGE.
        for (Long followerId : followerIds) {
            // 1) Key checks that should never really happen:
            if (followerId == null || postId == null || authorId == null) {
                // If any of these core IDs really is null, skip this follower instead of
                // throwing:
                continue;
            }
            if (score == null) {
                // If score is null, we cannot ZADD. Skip this follower too:
                continue;
            }

            // ————————————
            // 2) Add to the follower’s sorted set:
            // ZADD feed:user:<followerId> <score> <postId>
            String zsetKey = "feed:user:" + followerId;
            redis.opsForZSet().add(zsetKey, postId.toString(), score);

            // ————————————
            // 3) Build (or refresh) the Redis Hash for this post’s details:
            String postKey = "post:" + postId;
            // authorId is never null here:
            redis.opsForHash().put(postKey, "authorId", authorId.toString());

            // authorName might be null: store empty string if so
            String safeAuthorName = (authorName == null ? "" : authorName);
            redis.opsForHash().put(postKey, "authorName", safeAuthorName);

            // authorAvatarUrl might be null: store empty string if so
            String safeAvatar = (authorAvatar == null ? "" : authorAvatar);
            redis.opsForHash().put(postKey, "authorAvatarUrl", safeAvatar);

            // snippet (content snippet) might be null: store empty string if so
            String safeSnippet = (snippet == null ? "" : snippet);
            redis.opsForHash().put(postKey, "content", safeSnippet);

            // createdAt should never be null (you build it from post.getCreatedAt()), but
            // just in case:
            String createdAtStr = (event.getCreatedAt() == null
                    ? ""
                    : event.getCreatedAt().toString());
            redis.opsForHash().put(postKey, "createdAt", createdAtStr);

            // mediaUrls might be null or empty → represent as empty CSV string
            String csvMedia;
            if (mediaUrls == null || mediaUrls.isEmpty()) {
                csvMedia = "";
            } else {
                // If any element inside mediaUrls is null, filter it out first:

                List<String> safeList = mediaUrls.stream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                csvMedia = String.join(",", safeList);
            }
            redis.opsForHash().put(postKey, "mediaUrls", csvMedia);

            // Initialize the counts (always strings)
            redis.opsForHash().put(postKey, "likeCount", "0");
            redis.opsForHash().put(postKey, "commentCount", "0");
            redis.opsForHash().put(postKey, "shareCount", "0");

            // 4) Optionally set a TTL so old posts auto‐expire:
            redis.expire(postKey, Duration.ofDays(7));
        }

        System.out.println("Post fan-out completed for postId: " + postId);
    }
}
