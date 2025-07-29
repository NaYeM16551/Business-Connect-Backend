package com.example.demo.service.Posts;

import java.io.IOException;
import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.Posts.CommentResponseDto;
import com.example.demo.dto.Posts.PostCreatedEventDto;
import com.example.demo.dto.Posts.PostDto;
import com.example.demo.dto.Posts.PostMediaDto;
import com.example.demo.model.User;
import com.example.demo.model.Posts.Post;
import com.example.demo.model.Posts.PostComment;
import com.example.demo.model.Posts.PostLike;
import com.example.demo.model.Posts.PostMedia;
import com.example.demo.repository.UserRepository; // Assuming you have a UserRepository to fetch user details
import com.example.demo.repository.Posts.PostCommentRepository; // Assuming you have a PostCommentRepository
import com.example.demo.repository.Posts.PostLikeRepository;
import com.example.demo.repository.Posts.PostMediaRepository;
import com.example.demo.repository.Posts.PostRepository;
import com.example.demo.service.CloudinaryService;
import com.example.demo.service.RedisHealthService;

import jakarta.annotation.PostConstruct;

@Service
public class PostService {

    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private PostRepository postRepo;
    @Autowired
    private PostMediaRepository mediaRepo;

    @Autowired
    private PostLikeRepository postLikeRepo;

    @Autowired
    private UserRepository userRepo; // Assuming you have a UserRepository to fetch user details

    @Autowired
    private PostCommentRepository postCommentRepo; // Assuming you have a PostCommentRepository

    @Autowired
    private ApplicationEventPublisher eventPublisher; // For publishing events

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedisHealthService redisHealthService;

    private boolean isRedisAvailable = false;

    @PostConstruct
    public void checkRedisConnection() {
        // Use the centralized health service
        isRedisAvailable = redisHealthService.testRedisWithRetry(3);
        if (isRedisAvailable) {
            System.out.println("✅ Redis connection successful via health service");
        } else {
            System.err.println("❌ Redis connection failed after retries");
            System.err.println("Service will continue without Redis cache");
        }
    }

    // Helper method to safely execute Redis operations
    private void safeRedisOperation(Runnable operation) {
        // Check health before operations
        if (!redisHealthService.isRedisHealthy()) {
            isRedisAvailable = false;
            return;
        }

        try {
            operation.run();
            isRedisAvailable = true; // Mark as available on success
        } catch (Exception e) {
            System.err.println("❌ Redis operation failed: " + e.getMessage());
            isRedisAvailable = false; // Mark as unavailable for future operations
            // Force a health check to update the central service
            redisHealthService.forceHealthCheck();
        }
    }

    // Helper method to safely execute Redis operations with return value
    private <T> T safeRedisOperation(java.util.function.Supplier<T> operation, T defaultValue) {
        if (!isRedisAvailable)
            return defaultValue;
        try {
            return operation.get();
        } catch (Exception e) {
            System.err.println("❌ Redis operation failed: " + e.getMessage());
            isRedisAvailable = false;
            return defaultValue;
        }
    }

    private void updatePostCache(Post post) {
        safeRedisOperation(() -> {
            String postKey = "post:" + post.getId();
            redisTemplate.opsForHash().put(postKey, "content", post.getContent());
            if (post.getMedia() != null && !post.getMedia().isEmpty()) {
                redisTemplate.opsForHash().put(postKey, "mediaUrls",
                        post.getMedia().stream().map(PostMedia::getMediaUrl).collect(Collectors.joining(",")));
            }
            redisTemplate.expire(postKey, Duration.ofDays(7));
            System.out.println("✅ Redis cache updated for post: " + post.getId());
        });
    }

    @Transactional
    public Long createPost(String content, MultipartFile[] files, Principal principal) throws IOException {
        Long userId = Long.valueOf(principal.getName());
        System.out.println("Creating post for user ID: " + userId);

        Post post = new Post();
        post.setContent(content);
        post.setCreatedAt(java.time.LocalDateTime.now());

        // Set User (only set ID for reference)
        User user = new User();
        user.setId(userId);
        post.setUser(user);

        post = postRepo.save(post);

        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                String url = cloudinaryService.uploadFile(file);
                System.out.println("Uploaded file URL: " + url);

                PostMedia media = new PostMedia();
                media.setMediaUrl(url);
                media.setMediaType(file.getContentType());
                media.setPost(post);

                post.getMedia().add(media);
            }
        }

        // Try to update cache, but don't fail if Redis is unavailable
        try {
            updatePostCache(post);
        } catch (Exception e) {
            System.err.println("Cache update failed, continuing without cache: " + e.getMessage());
        }

        // Try to trigger event listener, but don't fail if Redis is down
        // try {
        // triggerEventListerner(post);
        // } catch (Exception e) {
        // System.err.println("Event trigger failed: " + e.getMessage());
        // }
        triggerEventListerner(post);

        return post.getId();
    }

    @Transactional
    public Long createGroupPost(String content, MultipartFile[] files, Long userId, Long groupId) throws IOException {
        System.out.println("Creating group post for user ID: " + userId + " in group ID: " + groupId);

        Post post = new Post();
        post.setContent(content);
        post.setCreatedAt(java.time.LocalDateTime.now());

        // Set User (only set ID for reference)
        User user = new User();
        user.setId(userId);
        post.setUser(user);

        // Set Group (only set ID for reference)
        com.example.demo.model.Groups.Group group = new com.example.demo.model.Groups.Group();
        group.setId(groupId);
        post.setGroup(group);

        post = postRepo.save(post);

        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                String url = cloudinaryService.uploadFile(file);
                System.out.println("Uploaded file URL: " + url);

                PostMedia media = new PostMedia();
                media.setMediaUrl(url);
                media.setMediaType(file.getContentType());
                media.setPost(post);

                post.getMedia().add(media);
            }
        }
        //triggerEventListerner(post);
        return post.getId();
    }

    @Transactional
    public PostDto getPostsByPostId(Long postId) {
        Post post = postRepo.findByIdWithMedia(postId).orElse(null); // Standard method
        if (post == null) {
            throw new RuntimeException("Post not found");
        }

        // Check Redis cache first
        String postKey = "post:" + postId;
        Map<Object, Object> cached = safeRedisOperation(() -> redisTemplate.opsForHash().entries(postKey),
                Collections.emptyMap());

        if (cached != null && !cached.isEmpty()) {
            // Build DTO from Redis hash
            PostDto postDto = new PostDto();
            postDto.setId(postId);
            postDto.setContent((String) cached.get("content"));
            postDto.setCreatedAt(LocalDateTime.parse((String) cached.get("createdAt")));
            postDto.setLikeCount(Long.parseLong((String) cached.getOrDefault("likeCount", "0")));
            postDto.setCommentCount(Long.parseLong((String) cached.getOrDefault("commentCount", "0")));
            postDto.setShareCount(Long.parseLong((String) cached.getOrDefault("shareCount", "0")));

            // Handle mediaUrls
            String mediaCsv = (String) cached.get("mediaUrls");
            if (mediaCsv != null && !mediaCsv.isEmpty()) {
                List<PostMediaDto> media = Arrays.stream(mediaCsv.split(","))
                        .map(url -> {
                            PostMediaDto m = new PostMediaDto();
                            m.setMediaUrl(url);
                            m.setMediaType(""); // If needed, fetch from DB or cache as well
                            return m;
                        })
                        .toList();
                postDto.setMedia(media);
            } else {
                postDto.setMedia(Collections.emptyList());
            }

            System.out.println("Post fetched from Redis cache: " + postKey);
            return postDto;
        }

        PostDto postDto = convertToDto(post);

        // postDto.setContent(post.getContent());
        // postDto.setCreatedAt(post.getCreatedAt());
        // postDto.setLikeCount((long) post.getLikes().size());
        // postDto.setShareCount(post.getShareCount());
        // postDto.setCommentCount((long) post.getComments().size());
        // if (post.getMedia() != null) {
        // List<PostMediaDto> mediaDtos = post.getMedia().stream()
        // .map(media -> {
        // PostMediaDto mediaDto = new PostMediaDto();
        // mediaDto.setMediaUrl(media.getMediaUrl());
        // mediaDto.setMediaType(media.getMediaType());
        // return mediaDto;
        // })
        // .toList();
        // postDto.setMedia(mediaDtos);
        // } else {
        // postDto.setMedia(Collections.emptyList());
        // }

        // save to Redis cache
        safeRedisOperation(() -> {
            redisTemplate.opsForHash().put(postKey, "content", postDto.getContent());
            redisTemplate.opsForHash().put(postKey, "createdAt", postDto.getCreatedAt().toString());
            redisTemplate.opsForHash().put(postKey, "likeCount", String.valueOf(postDto.getLikeCount()));
            redisTemplate.opsForHash().put(postKey, "commentCount", String.valueOf(postDto.getCommentCount()));
            redisTemplate.opsForHash().put(postKey, "shareCount", String.valueOf(postDto.getShareCount()));
            redisTemplate.opsForHash().put(postKey, "mediaUrls", String.join(",", postDto.getMedia().stream()
                    .map(PostMediaDto::getMediaUrl)
                    .toList()));
            redisTemplate.expire(postKey, Duration.ofDays(7));
        });

        return postDto;
    }

    public List<PostDto> getPostsByUserId(Long userId) {

        List<Post> posts = postRepo.getPostsByUserId(userId).orElse(Collections.emptyList());
        return posts.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PostDto> getPostsByGroupId(Long groupId, int page, int size) {
        List<Post> allPosts = postRepo.findAll();
        List<Post> groupPosts = allPosts.stream()
            .filter(post -> post.getGroup() != null && post.getGroup().getId().equals(groupId))
            .sorted(Comparator.comparing(Post::getCreatedAt).reversed())
            .skip((long) page * size)
            .limit(size)
            .toList();

        // Still in a transaction / Hibernate session, so lazy media will load
        return groupPosts.stream()
                         .map(this::convertToDto)
                         .toList();
    }

    private PostDto convertToDto(Post post) {
        PostDto postDto = new PostDto();
        postDto.setId(post.getId());
        postDto.setContent(post.getContent());
        postDto.setCreatedAt(post.getCreatedAt());

        if (post.getMedia() != null) {
            List<PostMediaDto> mediaDtos = post.getMedia().stream()
                    .map(media -> {
                        PostMediaDto mediaDto = new PostMediaDto();
                        mediaDto.setMediaUrl(media.getMediaUrl());
                        mediaDto.setMediaType(media.getMediaType());
                        return mediaDto;
                    })
                    .toList();
            postDto.setMedia(mediaDtos);
        } else {
            postDto.setMedia(Collections.emptyList());
        }

        return postDto;

    }

    @Transactional
    public void deletePost(Long postId, Long userId) {
        // 1) Load the post
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 2) Ensure this post belongs to the requesting user
        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own posts");
        }

        postRepo.unsetParentForChildren(postId);

        // 3) For each media entry, delete its file from Cloudinary
        List<PostMedia> mediaList = post.getMedia();
        post.getMedia().clear(); // Clear the in-memory list to avoid orphan removal issues
        for (PostMedia pm : mediaList) {
            String mediaUrl = pm.getMediaUrl();
            System.out.println("Deleting media from Cloudinary: " + mediaUrl);

            try {
                cloudinaryService.deleteFile(extractPublicIdFromUrl(mediaUrl), guessResourceTypeFromUrl(mediaUrl));
            } catch (IOException e) {
                // You can choose to log or rethrow. If you rethrow, the entire transaction will
                // rollback.
                throw new RuntimeException("Failed to delete media from Cloudinary: " + mediaUrl, e);
            }
        }

        // 4) Finally delete the post itself.
        // Because you used CascadeType.ALL + orphanRemoval=true on the Post→PostMedia
        // mapping,
        // you could simply do postRepo.delete(post) instead of
        // mediaRepo.deleteAll(...).
        // But since we dont explicitly called mediaRepo.deleteAll(...), now just delete
        // the
        // post.
        postRepo.delete(post);
        String postKey = "post:" + postId;
        safeRedisOperation(() -> redisTemplate.delete(postKey));

    }

    @Transactional
    public void likePost(Long postId, Long userId, Integer likeType) {
        Post post = postRepo.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));

        PostLike like = postLikeRepo.findByPostIdAndUserId(postId, userId).orElse(null);

        boolean flag = false;

        if (like == null) {
            like = new PostLike();
            like.setPost(post);
            like.setUser(userRepo.getReferenceById(userId)); // more robust than just new User().setId()
            flag = true;
        }
        like.setLikeType(likeType);
        like.setLikedAt(LocalDateTime.now());
        if (flag)
            post.getLikes().add(like); // add to post's likes collection

        // -----------------------------
        // 1) Build the Redis keys
        // -----------------------------
        String likeHashKey = "post:" + postId + ":likes"; // e.g. "post:123:likes"
        String postKey = "post:" + postId; // e.g. "post:123" (the main hash holding counts)
        String userIdStr = userId.toString();
        String newTypeStr = String.valueOf(likeType); // e.g. "2" for a “like” reaction

        // -----------------------------
        // 2) Fetch Old Reaction (if any)

        System.out.println(likeHashKey);
        System.out.println(userIdStr);
        Object oldTypeObj = safeRedisOperation(() -> redisTemplate.opsForHash().get(likeHashKey, userIdStr), null);
        String oldTypeStr = (oldTypeObj == null) ? null : (String) oldTypeObj;

        // print to debug

        System.out.println(oldTypeStr);

        // -----------------------------
        // 3) Decide what to do based on old vs. new
        // -----------------------------
        if (likeType == 0) {
            // The user is “un-reacting” (no reaction). If there was an old reaction, remove
            // it.
            if (oldTypeStr != null) {
                // 3a) Remove the field from the reaction‐hash
                safeRedisOperation(() -> redisTemplate.opsForHash().delete(likeHashKey, userIdStr));
                // 3b) Decrement the total likeCount in the main post hash
                safeRedisOperation(() -> redisTemplate.opsForHash().increment(postKey, "likeCount", -1));
            }
            // If oldTypeStr was already null (no prior reaction), do nothing.
        } else {
            // The user is reacting with a non-zero type (1..5).
            if (oldTypeStr == null || oldTypeStr.equals("0")) {
                // 3c) Brand-new reaction → increment total
                redisTemplate.opsForHash().put(likeHashKey, userIdStr, newTypeStr);
                redisTemplate.opsForHash().increment(postKey, "likeCount", 1);
            } else if (!oldTypeStr.equals(newTypeStr)) {
                // 3d) User is switching reaction from e.g. “love (1)” to “wow (3)”
                // Just overwrite the field, no change to likeCount
                redisTemplate.opsForHash().put(likeHashKey, userIdStr, newTypeStr);
            }
            // else: oldTypeStr.equals(newTypeStr) → user clicked the same reaction again;
            // do nothing
        }

        // After like logic, increment interaction if not self-like
        incrementInteractionCountIfNeeded(userId, post.getUser().getId());

        System.out.println("osadharon");

    }

    @Transactional
    public Long commentOnPost(
            Long postId,
            Long userId,
            String commentText,
            Long parentCommentId // ← new (optional) parameter
    ) {
        // 1) Ensure the post exists
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 2) If parentCommentId is provided, load and verify it
        PostComment parent = null;
        if (parentCommentId >= 0) {
            parent = postCommentRepo.findById(parentCommentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            // Optional: ensure the “parent” actually belongs to the same post
            if (!parent.getPost().getId().equals(postId)) {
                throw new RuntimeException("Parent comment does not belong to this post");
            }
        }

        // 3) Build the new PostComment
        PostComment newComment = new PostComment();
        newComment.setPost(post);
        newComment.setUser(userRepo.getReferenceById(userId));
        newComment.setContent(commentText);
        newComment.setCommentedAt(LocalDateTime.now());

        // 4) If this is a reply, wire in the parent
        if (parent != null) {
            newComment.setParentComment(parent);
            // Add this new comment to the parent's replies collection
            parent.getReplies().add(newComment);

        }

        // if (parent != null) {
        // parent.getReplies().add(newComment);
        // postCommentRepo.save(parent);
        // }

        // 5) Save only the new comment. JPA will cascade properly (if you’ve set
        // cascade on PostComment).
        newComment = postCommentRepo.save(newComment);

        // commenting owns post doesn't increment ranking
        String postKey = "post:" + postId;
        if (!userId.equals(post.getUser().getId()))
            safeRedisOperation(() -> redisTemplate.opsForHash().increment(postKey, "commentCount", 1));

        // After comment logic, increment interaction if not self-comment
        incrementInteractionCountIfNeeded(userId, post.getUser().getId());

        return newComment.getId();
    }

    @Transactional
    public List<CommentResponseDto> getCommentsByPostId(Long postId) {
        // 1) Ensure the post exists (throws if not)
        postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 2) Fetch all comments (top‐level + replies) for this post in one go
        List<PostComment> allComments = postCommentRepo.findByPostId(postId);

        // 3) Filter just the “top‐level” comments (those without a parent)
        List<PostComment> topLevel = allComments.stream()
                .filter(c -> c.getParentComment() == null)
                .toList();

        // 4) Map each top‐level comment to a DTO (including its immediate replies)
        return topLevel.stream()
                .map(comment -> {
                    // Build authorName safely (in case user was deleted)
                    String authorName = comment.getUser() != null
                            ? comment.getUser().getUsername()
                            : "Unknown";

                    // Build DTOs for immediate replies
                    List<CommentResponseDto> replyDtos = comment.getReplies().stream()
                            .map(reply -> {
                                String replyAuthor = reply.getUser() != null
                                        ? reply.getUser().getUsername()
                                        : "Unknown";

                                return new CommentResponseDto(
                                        replyAuthor,
                                        reply.getCommentedAt(),
                                        reply.getContent(),
                                        reply.getId(),
                                        reply.getParentComment() != null
                                                ? reply.getParentComment().getId()
                                                : null,
                                        /* no deeper nesting: */ Collections.emptyList());
                            })
                            .toList();

                    // Now build the top‐level DTO, passing in its list of replyDTOs
                    return new CommentResponseDto(
                            authorName,
                            comment.getCommentedAt(),
                            comment.getContent(),
                            comment.getId(),
                            /* top‐level has no parent: */ null,
                            replyDtos);
                })
                .toList();
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId, Long userId) {
        // 1) Load the comment (or throw if not found)
        PostComment comment = postCommentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));

        // 2) Verify it belongs to the specified post
        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("Comment does not belong to this post");
        }

        // 3) Verify the current user is the author of that comment
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You are not allowed to delete this comment");
        }

        comment.getReplies().size();

        // 4) Delete the comment. Because of cascade & orphanRemoval on 'replies',
        // any replies (children) of this comment will be deleted automatically.
        postCommentRepo.delete(comment);

        // 5) Decrement the comment count in Redis
        String postKey = "post:" + postId;
        safeRedisOperation(() -> redisTemplate.opsForHash().increment(postKey, "commentCount", -1));

    }

    @Transactional
    public void editPost(Long postId,
            String content,
            MultipartFile[] files,
            Long userId) throws IOException {

        // 1) Fetch the Post and verify it exists
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // 2) Check ownership: only the author can edit
        // Assuming Post.getUser().getId() returns the author’s user ID
        if (post.getUser() == null || !post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only edit your own posts");
        }

        // 3) Update the post’s textual content
        post.setContent(content);

        // 4) If there are existing media attachments, remove them
        // (Because we set cascade = ALL and orphanRemoval = true on the OneToMany side,
        // removing from post.getMedia() and saving should delete children from DB.)
        //
        // If you also want to delete the files themselves from Cloudinary, you would
        // need
        // to call your CloudinaryService.delete(...) method here for each URL. If you
        // do not
        // delete them from Cloudinary, they will remain in your Cloudinary account (you
        // can
        // add deletion logic if you track the public_id).
        //
        List<PostMedia> existingMedia = post.getMedia();
        if (!existingMedia.isEmpty()) {
            Iterator<PostMedia> iterator = existingMedia.iterator();
            while (iterator.hasNext()) {
                PostMedia pm = iterator.next();
                // Optionally: delete from Cloudinary if you have a method for that
                String publicId = extractPublicIdFromUrl(pm.getMediaUrl());
                cloudinaryService.deleteFile(publicId, guessResourceTypeFromUrl(pm.getMediaUrl())); // "auto" handles
                                                                                                    // both images and
                                                                                                    // videos

                // Remove from the Post’s media list (orphanRemoval = true will ensure DB
                // deletion)
                iterator.remove();
            }
        }

        // 5) Now persist the updated Post (text + no media). Because cascade=ALL +
        // orphanRemoval=true,
        // all old PostMedia rows will be deleted in the same transaction.
        postRepo.save(post);

        // 6) If new files were provided, upload each to Cloudinary and create a new
        // PostMedia entry
        if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                // a) Upload to Cloudinary (this returns a URL like https://…)
                String uploadedUrl = cloudinaryService.uploadFile(file);

                // b) Create a new PostMedia instance and link it
                PostMedia newMedia = new PostMedia();
                newMedia.setMediaUrl(uploadedUrl);
                // store the MIME type so you know if it’s image/jpeg, video/mp4, etc.
                newMedia.setMediaType(file.getContentType());
                newMedia.setPost(post);

                // c) Persist the new media row
                mediaRepo.save(newMedia);

                // d) Also add it to post.getMedia() so the in‐memory Post reflects the new
                // children
                post.getMedia().add(newMedia);
            }
        }

        // 7) Finally, save the Post again to update its relationships.
        // If you annotate this method with @Transactional, you could actually get away
        // with
        // not calling save(post) because Hibernate will flush on transaction commit.
        // But calling save(post) explicitly here is fine and ensures changes are queued
        // up.
        postRepo.save(post);

        // 8) Update the Redis cache for this post
        updatePostCache(post);
    }

    private String guessResourceTypeFromUrl(String url) {
        String lower = url.toLowerCase();
        if (lower.endsWith(".mp4") || lower.endsWith(".mov")
                || lower.endsWith(".avi") || lower.endsWith(".wmv")
                || lower.endsWith(".mkv")) {
            return "video";
        }
        // Otherwise assume image
        return "image";
    }

    /**
     * Given a Cloudinary URL, returns the public ID (everything after the version
     * number
     * and before the file extension). For example:
     *
     * Input:
     * "https://res.cloudinary.com/dv7lfz0nc/video/upload/v1748669766/gpaxntrzepz2jw6znhy7.mp4"
     * Output: "gpaxntrzepz2jw6znhy7"
     */
    public static String extractPublicIdFromUrl(String url) {
        // 1) Find the “/upload/” marker
        int uploadIndex = url.indexOf("/upload/");
        if (uploadIndex < 0) {
            throw new IllegalArgumentException("URL does not look like a Cloudinary upload URL: " + url);
        }

        // 2) Everything after “/upload/” is: "v1748669766/gpaxntrzepz2jw6znhy7.mp4"
        String afterUpload = url.substring(uploadIndex + "/upload/".length());

        // 3) The first slash after the version number separates “v1748669766” from the
        // public ID + extension
        int slashAfterVersion = afterUpload.indexOf('/');
        if (slashAfterVersion < 0) {
            // No slash found—unlikely for a valid upload URL, but guard anyway
            throw new IllegalArgumentException("Cannot find version separator in URL: " + url);
        }

        // 4) Grab everything after "v1748669766/" → "gpaxntrzepz2jw6znhy7.mp4"
        String publicIdWithExtension = afterUpload.substring(slashAfterVersion + 1);

        // 5) Strip off the file extension (everything after the last “.”)
        int lastDot = publicIdWithExtension.lastIndexOf('.');
        if (lastDot < 0) {
            // No extension: return the whole string
            return publicIdWithExtension;
        }

        // 6) Return only "gpaxntrzepz2jw6znhy7"
        return publicIdWithExtension.substring(0, lastDot);
    }

    @Transactional
    public PostDto getPostForFeed(Long postId) {
        String postKey = "post:" + postId;
        // Try to fetch from Redis
        Map<Object, Object> cached = safeRedisOperation(() -> redisTemplate.opsForHash().entries(postKey),
                Collections.emptyMap());

        if (cached != null && !cached.isEmpty()) {
            // Build DTO from Redis hash
            PostDto dto = new PostDto();
            dto.setId(postId);
            dto.setContent((String) cached.get("content"));
            dto.setCreatedAt(LocalDateTime.parse((String) cached.get("createdAt")));
            dto.setLikeCount(Long.parseLong((String) cached.getOrDefault("likeCount", "0")));
            dto.setCommentCount(Long.parseLong((String) cached.getOrDefault("commentCount", "0")));
            dto.setShareCount(Long.parseLong((String) cached.getOrDefault("shareCount", "0")));

            // Handle mediaUrls
            String mediaCsv = (String) cached.get("mediaUrls");
            if (mediaCsv != null && !mediaCsv.isEmpty()) {
                List<PostMediaDto> media = Arrays.stream(mediaCsv.split(","))
                        .map(url -> {
                            PostMediaDto m = new PostMediaDto();
                            m.setMediaUrl(url);
                            m.setMediaType(""); // If needed, fetch from DB or cache as well
                            return m;
                        })
                        .toList();
                dto.setMedia(media);
            } else {
                dto.setMedia(Collections.emptyList());
            }
            return dto;
        }

        // Fallback to DB (and repopulate Redis for next time)
        Post post = postRepo.findByIdWithMedia(postId).orElse(null);
        if (post == null)
            throw new RuntimeException("Post not found");

        PostDto dto = convertToDto(post);

        // Repopulate Redis (optional: handle in async/background for massive scale)
        safeRedisOperation(() -> {
            redisTemplate.opsForHash().put(postKey, "content", dto.getContent());
            redisTemplate.opsForHash().put(postKey, "createdAt", dto.getCreatedAt().toString());
            redisTemplate.opsForHash().put(postKey, "likeCount", String.valueOf(dto.getLikeCount()));
            redisTemplate.opsForHash().put(postKey, "commentCount", String.valueOf(dto.getCommentCount()));
            redisTemplate.opsForHash().put(postKey, "shareCount", String.valueOf(dto.getShareCount()));
            if (dto.getMedia() != null && !dto.getMedia().isEmpty()) {
                redisTemplate.opsForHash().put(postKey, "mediaUrls",
                        dto.getMedia().stream().map(PostMediaDto::getMediaUrl).collect(Collectors.joining(",")));
            }
            redisTemplate.expire(postKey, Duration.ofDays(7));
        });

        return dto;
    }

    @Transactional
    public void editComment(Long postId, Long commentId, Long userId, String newContent) {
        // 1) Ensure the post exists. (We only need this check to guarantee the postId
        // is valid.)
        postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id = " + postId));

        // 2) Load the comment
        PostComment comment = postCommentRepo.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found with id = " + commentId));

        // 3) Verify that this comment actually belongs to the specified post
        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException(
                    "Comment with id = " + commentId + " does not belong to post with id = " + postId);
        }

        // 4) Verify that the currently authenticated user is indeed the author of the
        // comment
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You can only edit your own comments");
        }

        // 5) Update the comment’s content
        comment.setContent(newContent);

        // 6) Save the updated comment
        postCommentRepo.save(comment);
    }

    @Transactional
    public void triggerEventListerner(Post post) {
        // 1) Convert LocalDateTime to Instant (UTC), needed for Redis scoring
        Instant createdInstant = post.getCreatedAt()
                .atZone(ZoneOffset.systemDefault())
                .toInstant();

        String snippet = post.getContent().length() <= 200
                ? post.getContent()
                : post.getContent().substring(0, 200);

        PostCreatedEventDto eventDto = new PostCreatedEventDto(
                post.getId(),
                post.getUser().getId(),
                createdInstant,
                snippet,
                post.getMedia().stream()
                        .map(PostMedia::getMediaUrl)
                        .toList());

        // 2) Publish the event to notify other components (e.g., Redis, search index)
        System.out.println("Publishing PostCreatedEvent for post ID: " + post.getId());
        eventPublisher.publishEvent(eventDto);
    }

    @Transactional
    public Long sharePost(Long postId, Long userId, String shareContent) {
        // 1) Load the original post
        Post originalPost = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        // 2) Load the user who is doing the share
        User sharingUser = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 3) Create a new Post entity representing the "shared" post
        Post sharedPost = new Post();
        sharedPost.setUser(sharingUser);

        // Copy the content from original → you can also choose to leave it blank
        sharedPost.setContent(shareContent);

        // Set the timestamp for when the share happened
        sharedPost.setCreatedAt(LocalDateTime.now());

        boolean originalParentExist = originalPost.getParentPostId() != null && originalPost.getParentPostId() != -1;
        Post originalParentPost = null;
        if (originalParentExist) {
            originalParentPost = postRepo.findById(originalPost.getParentPostId())
                    .orElseThrow(() -> new IllegalArgumentException("Original parent post not found"));
            originalParentPost.incrementShareCount();
            postRepo.save(originalParentPost);

        }
        // Mark this new post as a “child” of the original
        sharedPost.setParentPostId(originalParentExist ? originalPost.getParentPostId() : originalPost.getId());

        // A brand‐new shared post starts with zero shares of its own
        sharedPost.setShareCount(0L);

        // 4) Persist the new "shared" post
        postRepo.save(sharedPost);

        // 5) Increment the original’s shareCount and persist
        originalPost.incrementShareCount();
        postRepo.save(originalPost);

        String postKey = "post:" + postId;
        if (!userId.equals(originalPost.getUser().getId()))
            safeRedisOperation(() -> redisTemplate.opsForHash().increment(postKey, "shareCount", 1));

        // 6) Optionally set a TTL so old posts auto‐expire:
        safeRedisOperation(() -> redisTemplate.expire(postKey, Duration.ofDays(7)));

        // 7) Trigger the event listener to update feeds and caches
        triggerEventListerner(sharedPost); // This will publish the PostCreatedEvent

        // 5.1) Store parent post info in Redis for the shared post (for feed display)
        String sharedPostKey = "post:" + sharedPost.getId();
        // Parent info: parentPostId, parentAuthorId, parentAuthorName,
        // parentAuthorAvatarUrl
        Long parentPostId = sharedPost.getParentPostId();
        {
            Post parentPost = originalParentExist ? originalParentPost : originalPost;
            if (parentPost != null && parentPost.getUser() != null) {
                safeRedisOperation(() -> {
                    redisTemplate.opsForHash().put(sharedPostKey, "parentPostId", String.valueOf(parentPostId));
                    redisTemplate.opsForHash().put(sharedPostKey, "parentAuthorId",
                            String.valueOf(parentPost.getUser().getId()));
                    redisTemplate.opsForHash().put(sharedPostKey, "parentAuthorName",
                            parentPost.getUser().getUsername() != null ? parentPost.getUser().getUsername() : "");
                    redisTemplate.opsForHash().put(sharedPostKey, "parentAuthorAvatarUrl",
                            parentPost.getUser().getProfilePictureUrl() != null
                                    ? parentPost.getUser().getProfilePictureUrl()
                                    : "");
                    redisTemplate.opsForHash().put(sharedPostKey, "parentPostContentSnippet",
                            parentPost.getContent().length() <= 200
                                    ? parentPost.getContent()
                                    : parentPost.getContent().substring(0, 200));
                });
            }
        }

        // After share logic, increment interaction if not self-share
        incrementInteractionCountIfNeeded(userId, originalPost.getUser().getId());

        return sharedPost.getId();
    }

    // Helper: Increment interaction count in Redis if follower interacts with
    // followee
    private void incrementInteractionCountIfNeeded(Long actorUserId, Long postAuthorId) {
        if (actorUserId == null || postAuthorId == null || actorUserId.equals(postAuthorId))
            return;
        String key = "interaction:" + actorUserId + "," + postAuthorId;
        safeRedisOperation(() -> redisTemplate.opsForValue().increment(key, 1));
    }

}
