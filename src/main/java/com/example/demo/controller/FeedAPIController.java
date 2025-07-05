package com.example.demo.controller;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.Feed.CursorDto;
import com.example.demo.dto.Feed.FeedItemDto;
import com.example.demo.dto.Feed.FeedPageResponseDto;

@RestController
@RequestMapping("/api/v1/feed")
public class FeedAPIController {

    private final StringRedisTemplate redis;

    // How many candidates to retrieve from Redis by timestamp, before dynamic
    // re‐scoring
    private static final int CANDIDATE_POOL_SIZE = 100;

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
            return 0L;
        }
    }

    /**
     * GET
     * /api/v1/feed?cursorScore=<score>&cursorPostId=<postId>&lastPostTime=<lastTime>&limit=<n>
     *
     * Returns a page of feed items for the currently authenticated user.
     *
     * 1) Fetch a pool of recent posts by *raw timestamp*.
     * 2) Compute a dynamic "rankScore" in‐Java.
     * 3) Sort by (rankScore DESC, postId DESC).
     * 4) Apply dynamic cursor & limit in‐memory.
     * 5) Return result + nextCursor.
     */
    @GetMapping
    public ResponseEntity<FeedPageResponseDto> getFeed(
            @RequestParam(value = "cursorScore", required = false) Double cursorScore, // dynamic‐score cursor
            @RequestParam(value = "cursorPostId", required = false) Long cursorPostId,
            @RequestParam(value = "lastPostTime", required = false) String lastTime,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            Authentication authentication) {

        try {
            // Validate authentication
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.badRequest().body(new FeedPageResponseDto(Collections.emptyList(), null));
            }

            Long userId;
            try {
                userId = Long.valueOf(authentication.getName());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(new FeedPageResponseDto(Collections.emptyList(), null));
            }

            // Validate limit parameter
            if (limit <= 0 || limit > 100) {
                limit = 20; // Default to 20 if invalid
            }

            String cacheKey = buildCacheKey(userId, cursorScore, cursorPostId, limit);

            // 1) Try cached page
            FeedPageResponseDto cachedPage = getCachedFeedPage(cacheKey);
            if (cachedPage != null) {
                return ResponseEntity.ok(cachedPage);
            }
            System.out.println("Cache miss for key: " + cacheKey);

            // 2) Fetch a pool of candidates by *raw timestamp* (desc)
            String zsetKey = "feed:user:" + userId;
            Set<String> candidateIds = getFeedPostIds(zsetKey, CANDIDATE_POOL_SIZE);

            // 3) Build DTOs + compute a dynamic score for each
            List<FeedItemDto> scoredItems = buildFeedItemsWithDynamicScore(candidateIds, userId);

            if (scoredItems.size() == 0) {
                FeedPageResponseDto emptyPage = new FeedPageResponseDto(Collections.emptyList(), null);
                return ResponseEntity.ok(emptyPage);
            }

            // 4) Sort by (rankScore DESC, postId DESC)
            scoredItems.sort(
                    Comparator.comparing(FeedItemDto::getRankScore, Comparator.reverseOrder())
                            .thenComparing(FeedItemDto::getPostId, Comparator.reverseOrder()));

            // 5) Apply USER'S dynamic cursor entirely in memory
            List<FeedItemDto> pageItems = applyCursorAndLimit(scoredItems, cursorScore, cursorPostId, limit, lastTime);

            // 6) Build nextCursor from the last item we returned
            CursorDto nextCursor = buildNextCursor(pageItems);

            if (nextCursor != null)
                System.out.println(nextCursor.getLastDateTime());

            FeedPageResponseDto page = new FeedPageResponseDto(pageItems, nextCursor);

            // 7) Cache it for a short TTL
            cacheFeedPage(cacheKey, page);

            return ResponseEntity.ok(page);
        } catch (Exception e) {
            System.err.println("Error in getFeed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(new FeedPageResponseDto(Collections.emptyList(), null));
        }
    }

    // ———————————————————————————————
    // 1) CACHE KEY & CACHE HELPERS
    // ———————————————————————————————
    private String buildCacheKey(Long userId, Double cursorScore, Long cursorPostId, int limit) {
        String cScore = (cursorScore == null ? "start" : String.valueOf(cursorScore));
        String cPid = (cursorPostId == null ? "start" : cursorPostId.toString());
        return String.format("feed_json:user:%d:%s:%s:%d", userId, cScore, cPid, limit);
    }

    private FeedPageResponseDto getCachedFeedPage(String cacheKey) {
        try {
            String cachedJson = redis.opsForValue().get(cacheKey);
            if (cachedJson != null && !cachedJson.isBlank()) {
                return FeedPageResponseDto.fromJson(cachedJson);
            }
        } catch (Exception e) {
            System.err.println("Error reading from cache: " + e.getMessage());
        }
        return null;
    }

    private void cacheFeedPage(String cacheKey, FeedPageResponseDto page) {
        try {
            redis.opsForValue().set(cacheKey, page.toJson(), Duration.ofSeconds(15));
        } catch (Exception e) {
            System.err.println("Error caching feed page: " + e.getMessage());
        }
    }

    // ———————————————————————————————
    // 2) CANDIDATE FETCH BY RAW TIMESTAMP
    // ———————————————————————————————
    /**
     * Always fetch up to `candidatePoolSize` post IDs, sorted by raw timestamp
     * descending.
     * We store each post's createdAt epoch‐ms as the ZSET score.
     */
    private Set<String> getFeedPostIds(String zsetKey, int candidatePoolSize) {
        try {
            Set<String> postIdStrings = redis.opsForZSet().reverseRange(zsetKey, 0, candidatePoolSize - 1);
            if (postIdStrings == null) {
                return Collections.emptySet();
            }
            return postIdStrings;
        } catch (Exception e) {
            System.err.println("Error fetching feed post IDs: " + e.getMessage());
            return Collections.emptySet();
        }
    }

    // ———————————————————————————————
    // 3) BUILD DTOs WITH DYNAMIC SCORE
    // ———————————————————————————————
    /**
     * For each postId in the candidate pool:
     * 1) Fetch hash fields: createdAt, likeCount, commentCount, shareCount,
     * mediaUrls
     * 2) Compute recencyScore = exp(-hoursAgo/24)
     * 3) Compute engagementScore = log(1 + likeCount) + 0.5*log(1 + commentCount) +
     * 0.8*log(1 + shareCount)
     * 4) Compute mediaBoost = 0.1 if hasMedia else 0.0
     * 5) finalScore = 0.6*recencyScore + 0.3*engagementScore + mediaBoost
     */
    private List<FeedItemDto> buildFeedItemsWithDynamicScore(Set<String> postIdStrings, Long userId) {
        long nowMs = System.currentTimeMillis();
        List<FeedItemDto> items = new ArrayList<>();

        for (String pidStr : postIdStrings) {
            try {
                Long pid;
                try {
                    pid = Long.valueOf(pidStr);
                } catch (NumberFormatException ex) {
                    continue; // skip invalid
                }

                String postKey = "post:" + pid;
                Map<Object, Object> postHash;
                try {
                    postHash = redis.opsForHash().entries(postKey);
                } catch (Exception e) {
                    System.err.println("Error fetching post hash for " + pid + ": " + e.getMessage());
                    continue;
                }
                
                if (postHash == null || postHash.isEmpty()) {
                    continue;
                }

                // 1) Extract fields
                String createdAtStr = (String) postHash.get("createdAt");
                if (createdAtStr == null || createdAtStr.isBlank()) {
                    continue;
                }
                
                long createdAtMs;
                try {
                    createdAtMs = Instant.parse(createdAtStr).toEpochMilli();
                } catch (Exception e) {
                    continue; // skip if unparsable
                }

                long likeCount = safeLong(postHash.get("likeCount"));
                long commentCount = safeLong(postHash.get("commentCount"));
                long shareCount = safeLong(postHash.get("shareCount"));

                String mediaCsv = (String) postHash.get("mediaUrls");
                boolean hasMedia = (mediaCsv != null && !mediaCsv.isBlank());

                // 1.5) Fetch interaction count between user and author
                Long authorId = null;
                try {
                    String authorIdStr = (String) postHash.get("authorId");
                    if (authorIdStr != null && !authorIdStr.isBlank()) {
                        authorId = Long.valueOf(authorIdStr);
                    }
                } catch (Exception e) {
                    // Skip this post if authorId is invalid
                    continue;
                }
                
                long interactionCnt = 0L;
                if (authorId != null && !authorId.equals(userId)) {
                    try {
                        String interactionKey = "interaction:" + userId + "," + authorId;
                        String cntStr = redis.opsForValue().get(interactionKey);
                        if (cntStr != null && !cntStr.isBlank()) {
                            interactionCnt = Long.parseLong(cntStr);
                        } else {
                            interactionCnt = -10L; // Default to 0 if not found
                        }
                    } catch (Exception e) {
                        interactionCnt = -10L;
                    }
                }

                // 2) Compute recencyScore = exp(-hoursAgo/24)
                double hoursAgo = (nowMs - createdAtMs) / 3_600_000.0;
                double recencyScore = Math.exp(-hoursAgo / 24.0);

                // 3) Compute engagementScore
                double engagementScore = Math.log(1 + likeCount)
                        + 0.5 * Math.log(1 + commentCount)
                        + 0.8 * Math.log(1 + shareCount);

                // 4) mediaBoost
                double mediaBoost = hasMedia ? 1 : 0.0;

                // 4.5) interactionBoost
                double interactionBoost = interactionCnt >= 0 ? Math.log(1 + interactionCnt) : -5;

                // 5) finalScore
                double finalScore = 0.6 * recencyScore + 0.3 * engagementScore + 0.1 * mediaBoost + 0.2 * interactionBoost;

                System.out.printf(" userId %d:,Post %d: , finalScore=%.4f, interactionBoost=%f\n",
                        userId, pid, finalScore, interactionBoost);

                // 6) Build the DTO and set rankScore
                FeedItemDto dto = new FeedItemDto();
                dto.setPostId(pid);
                dto.setRankScore(finalScore);

                // 7) Populate the rest of dto from hash
                dto.setAuthorId(authorId);
                dto.setAuthorName((String) postHash.get("authorName"));
                dto.setAuthorAvatarUrl((String) postHash.get("authorAvatarUrl"));
                dto.setContentSnippet((String) postHash.get("content"));
                dto.setCreatedAt(createdAtStr);

                // Parent info for shares (null check)
                String parentPostIdStr = (String) postHash.get("parentPostId");
                if (parentPostIdStr != null && !parentPostIdStr.isBlank()) {
                    try {
                        dto.setParentPostId(Long.valueOf(parentPostIdStr));
                    } catch (Exception e) {
                        dto.setParentPostId(null);
                    }
                }
                String parentAuthorIdStr = (String) postHash.get("parentAuthorId");
                if (parentAuthorIdStr != null && !parentAuthorIdStr.isBlank()) {
                    try {
                        dto.setParentAuthorId(Long.valueOf(parentAuthorIdStr));
                    } catch (Exception e) {
                        dto.setParentAuthorId(null);
                    }
                }
                String parentAuthorName = (String) postHash.get("parentAuthorName");
                if (parentAuthorName != null)
                    dto.setParentAuthorName(parentAuthorName);
                String parentAuthorAvatarUrl = (String) postHash.get("parentAuthorAvatarUrl");
                if (parentAuthorAvatarUrl != null)
                    dto.setParentAuthorAvatarUrl(parentAuthorAvatarUrl);

                String parentPostContentSnippet = (String) postHash.get("parentPostContentSnippet");
                if (parentPostContentSnippet != null)
                    dto.setParentPostContentSnippet(parentPostContentSnippet);

                List<String> mediaList;
                if (mediaCsv == null || mediaCsv.isEmpty()) {
                    mediaList = List.of();
                } else {
                    mediaList = List.of(mediaCsv.split(","));
                }
                dto.setMediaUrls(mediaList);

                dto.setLikeCount(likeCount);
                dto.setCommentCount(commentCount);
                dto.setShareCount(shareCount);

                // 8) Fetch this user's reaction from post:<pid>:likes
                try {
                    String likeHashKey = "post:" + pid + ":likes";
                    Object likeTypeObj = redis.opsForHash().get(likeHashKey, userId.toString());
                    if (likeTypeObj != null) {
                        dto.setMyLikeType(Long.parseLong((String) likeTypeObj));
                    } else {
                        dto.setMyLikeType(0L);
                    }
                } catch (Exception e) {
                    dto.setMyLikeType(0L);
                }

                items.add(dto);
            } catch (Exception e) {
                System.err.println("Error processing post " + pidStr + ": " + e.getMessage());
                continue; // Skip this post and continue with others
            }
        }

        return items;
    }

    // ———————————————————————————————
    // 4) APPLY DYNAMIC CURSOR & LIMIT IN MEMORY
    // ———————————————————————————————
    /**
     * Given a sorted list of FeedItemDto (by DESC rankScore, then DESC postId),
     * apply cursor‐based pagination:
     *
     * • If no cursor, take first `limit` items.
     * • If cursor is provided, skip all items where
     * (dto.getRankScore() > cursorScore) OR
     * (dto.getRankScore() == cursorScore AND dto.getPostId() >= cursorPostId).
     * Then take up to `limit` from the remainder.
     */
    private List<FeedItemDto> applyCursorAndLimit(
            List<FeedItemDto> sortedItems,
            Double cursorScore,
            Long cursorPostId,
            int limit, String lastTime) {

        List<FeedItemDto> page = new ArrayList<>(limit);
        int count = 0;

        System.out.println("size " + sortedItems.size());

        Instant i1 = null;
        boolean useSessionRecency = false;
        if (lastTime != null && !lastTime.isBlank()) {
            try {
                i1 = Instant.parse(lastTime);
                useSessionRecency = true;
            } catch (Exception e) {
                useSessionRecency = false;
            }
        }

        for (FeedItemDto dto : sortedItems) {
            try {
                double score = dto.getRankScore();
                long pid = dto.getPostId();

                // Session recency: only add if createdAt > lastTime
                if (useSessionRecency && i1 != null) {
                    try {
                        Instant i2 = Instant.parse(dto.getCreatedAt());
                        if (i2.isAfter(i1)) {
                            page.add(dto);
                            count++;
                            if (count >= limit)
                                break;
                            continue;
                        }
                    } catch (Exception e) {
                        // If parsing fails, skip recency filter for this item
                    }
                }

                // If we have a cursor, skip until we find items strictly "after" the cursor
                if (cursorScore != null && cursorPostId != null) {
                    if ((score >= cursorScore || pid == cursorPostId)) {
                        continue;
                    } else if (score == cursorScore && pid <= cursorPostId) {
                        continue;
                    }
                }

                page.add(dto);
                count++;
                if (count >= limit)
                    break;
            } catch (Exception e) {
                System.err.println("Error processing item in cursor: " + e.getMessage());
                continue;
            }
        }

        return page;
    }

    // ———————————————————————————————
    // 5) BUILD NEXT CURSOR FROM LAST ITEM
    // ———————————————————————————————
    private CursorDto buildNextCursor(List<FeedItemDto> items) {
        try {
            if (!items.isEmpty()) {
                FeedItemDto lastItem = items.get(items.size() - 1);
                String lastTime = items.get(0).getCreatedAt();
                // Keep it as a Double
                return new CursorDto(lastItem.getRankScore() - 0.01, lastItem.getPostId(), lastTime);
            }
        } catch (Exception e) {
            System.err.println("Error building next cursor: " + e.getMessage());
        }
        return null;
    }
}
