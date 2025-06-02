package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.demo.dto.Feed.CursorDto;
import com.example.demo.dto.Feed.FeedItemDto;
import com.example.demo.dto.Feed.FeedPageResponseDto;

import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/api/v1/feed")
public class FeedAPIController {

    private final StringRedisTemplate redis;

    @Autowired
    public FeedAPIController(StringRedisTemplate redis) {
        this.redis = redis;

    }

    private static Long safeLong(Object val) {
        if (val == null)
            return 0L;
        String s = val.toString();
        if (s.isBlank())
            return 0L;
        try {
            return Long.valueOf(s);
        } catch (NumberFormatException ex) {
            return 0L; // or throw if you want
        }
    }

    /**
     * GET /api/v1/feed?cursorScore=<score>&cursorPostId=<postId>&limit=<n>
     *
     * Returns a page of feed items for the currently authenticated user.
     *
     * @param authHeader   “Authorization: Bearer <JWT_TOKEN>”
     * @param cursorScore  Optional: the last page’s lowest score (for pagination)
     * @param cursorPostId Optional: the last page’s post ID (break ties)
     * @param limit        How many items to return (default = 20)
     */
    @GetMapping
    public ResponseEntity<FeedPageResponseDto> getFeed(
            @RequestParam(value = "cursorScore", required = false) Long cursorScore,
            @RequestParam(value = "cursorPostId", required = false) Long cursorPostId,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            Authentication authentication

    ) {
        // With Spring Security, principal is guaranteed non-null (unless endpoint is
        // not secured).
        Long userId = Long.valueOf(authentication.getName());
        // —————————————————————————————
        // 2) Check for a cached JSON page in Redis
        // —————————————————————————————
        // Construct a unique key:
        // “feed_json:user:<userId>:<cursorScore>:<cursorPostId>:<limit>”
        String cScore = (cursorScore == null ? "start" : cursorScore.toString());
        String cPid = (cursorPostId == null ? "start" : cursorPostId.toString());
        String cacheKey = String.format("feed_json:user:%d:%s:%s:%d",
                userId, cScore, cPid, limit);

        String cachedJson = redis.opsForValue().get(cacheKey);
        if (cachedJson != null) {
            // If we have a cached JSON response, convert it back to our DTO and return
            // immediately
            FeedPageResponseDto cachedPage = FeedPageResponseDto.fromJson(cachedJson);
            return ResponseEntity.ok(cachedPage);
        }

        // —————————————————————————————
        // 3) Read from the user’s sorted set: “feed:user:<userId>”
        // —————————————————————————————
        String zsetKey = "feed:user:" + userId;
        Set<String> postIdStrings;

        if (cursorScore == null || cursorPostId == null) {
            // First page: get the top N items (highest scores first)
            postIdStrings = redis.opsForZSet().reverseRange(zsetKey, 0, limit - 1);
        } else {
            // Paginated request: get items with score < cursorScore
            //
            // NOTE: This is a simplified approach assuming “score” (timestamp ms) rarely
            // ties.
            // If tie-breaking is required, you might store a composite score (e.g.
            // timestamp * 1e6 + postId).
            double maxScore = cursorScore - 1; // strictly less than the last page’s last score
            postIdStrings = redis.opsForZSet()
                    .reverseRangeByScore(zsetKey, maxScore, 0, 0, limit);
        }

        if (postIdStrings == null) {
            postIdStrings = Collections.emptySet();
        }

        // —————————————————————————————
        // 4) For each postId, fetch its Hash (“post:<postId>”) for details
        // —————————————————————————————
        List<FeedItemDto> items = new ArrayList<>();
        long lastScoreVal = 0L;
        long lastPostIdVal = 0L;

        for (String pidStr : postIdStrings) {
            Long pid = Long.valueOf(pidStr);
            String postKey = "post:" + pid;

            // Get all fields from the Redis Hash
            Map<Object, Object> postHash = redis.opsForHash().entries(postKey);
            if (postHash.isEmpty() || postHash == null) {
                // If the hash expired or was never set, skip
                continue;
            }

            // Build a DTO for each feed item
            FeedItemDto dto = new FeedItemDto();
            dto.setPostId(pid);
            dto.setAuthorId(Long.valueOf((String) postHash.get("authorId")));
            dto.setAuthorName((String) postHash.get("authorName"));
            dto.setAuthorAvatarUrl((String) postHash.get("authorAvatarUrl"));
            dto.setContentSnippet((String) postHash.get("content"));
            dto.setCreatedAt((String) postHash.get("createdAt"));

            // Deserialize mediaUrls (CSV) into a List<String>
            String mediaCsv = (String) postHash.get("mediaUrls");
            List<String> mediaList;
            if (mediaCsv == null || mediaCsv.isEmpty()) {
                mediaList = List.of();
            } else {
                mediaList = List.of(mediaCsv.split(","));
            }
            dto.setMediaUrls(mediaList);

            // Populate counts
            dto.setLikeCount(safeLong(postHash.get("likeCount")));
            dto.setCommentCount(safeLong(postHash.get("commentCount")));
            dto.setShareCount(safeLong(postHash.get("shareCount")));

            // Fetch the “score” from the ZSET (the timestamp or rank)
            Double scoreDbl = redis.opsForZSet().score(zsetKey, pidStr);
            dto.setRankScore(scoreDbl == null ? 0.0 : scoreDbl);

            // 3) Now fetch this user’s reaction from the “likes” hash
            String likeHashKey = "post:" + pid + ":likes";
            Object likeTypeObj = redis.opsForHash().get(likeHashKey, userId.toString());
            if (likeTypeObj != null) {
                dto.setMyLikeType(Long.parseLong((String) likeTypeObj));
            } else {
                dto.setMyLikeType(0L); // 0 = no-react
            }

            // Initially, mark like/share flags as false; a real app would check if
            // this user has liked/shared this post, perhaps via another Redis set or DB.
            dto.setIsSharedByMe(false);

            items.add(dto);

            // Track the last item’s score & postId for “nextCursor”
            if (scoreDbl != null) {
                lastScoreVal = scoreDbl.longValue();
                lastPostIdVal = pid;
            }
        }

        // —————————————————————————————
        // 5) Build the next‐page cursor (if we have at least one item)
        // —————————————————————————————
        CursorDto nextCursor = null;
        if (!items.isEmpty()) {
            // The last item in our “items” list is the lowest‐scored of this page.
            FeedItemDto lastItem = items.get(items.size() - 1);
            nextCursor = new CursorDto(lastItem.getRankScore().longValue(),
                    lastItem.getPostId());
        }

        FeedPageResponseDto page = new FeedPageResponseDto(items, nextCursor);

        // —————————————————————————————
        // 6) Cache the JSON for this page under
        // “feed_json:user:<userId>:<cursor>:<limit>”
        // with a short TTL(15 sec) so repeated scrolls don’t hammer Redis.
        // —————————————————————————————
        redis.opsForValue().set(cacheKey, page.toJson(), Duration.ofSeconds(15));

        // Return the constructed page
        return ResponseEntity.ok(page);
    }
}
