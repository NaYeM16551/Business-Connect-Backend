package com.example.demo.service.Posts;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.Iterator;

import com.example.demo.dto.Posts.CommentResponseDto;
import com.example.demo.dto.Posts.PostDto;
import com.example.demo.dto.Posts.PostMediaDto;
import com.example.demo.model.User;
import com.example.demo.model.Posts.Post;
import com.example.demo.model.Posts.PostMedia;
import com.example.demo.model.Posts.PostLike;
import com.example.demo.model.Posts.PostComment;

import com.example.demo.repository.Posts.PostMediaRepository;
import com.example.demo.repository.Posts.PostRepository;
import com.example.demo.service.CloudinaryService;

import com.example.demo.repository.Posts.PostLikeRepository;

import com.example.demo.repository.Posts.PostCommentRepository; // Assuming you have a PostCommentRepository
import com.example.demo.repository.UserRepository; // Assuming you have a UserRepository to fetch user details

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

        if(files == null || files.length == 0) return post.getId();
        for (MultipartFile file : files) {
            String url = cloudinaryService.uploadFile(file);
            System.out.println("Uploaded file URL: " + url);

            PostMedia media = new PostMedia();
            media.setMediaUrl(url);
            media.setMediaType(file.getContentType()); // <-- Set mediaType!
            media.setPost(post);

            post.getMedia().add(media);
        }

        return post.getId();
    }
    
    @Transactional
    public PostDto getPostsByPostId(Long postId) {
        Post post = postRepo.findByIdWithMedia(postId).orElse(null); // Standard method
        if (post == null) {
            throw new RuntimeException("Post not found");
        }

        // System.out.println("Post found: " + post.getId() + ", Content: " +
        // post.getCreatedAt());

        PostDto postDto = new PostDto();
        postDto.setId(post.getId());
        postDto.setContent(post.getContent());
        postDto.setCreatedAt(post.getCreatedAt());
        postDto.setLikeCount((long) post.getLikes().size());
        postDto.setShareCount(post.getShareCount());
        postDto.setCommentCount((long) post.getComments().size());
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

    public List<PostDto> getPostsByUserId(Long userId) {

        List<Post> posts = postRepo.getPostsByUserId(userId).orElse(Collections.emptyList());
        return posts.stream()
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
        // But since we dont  explicitly called mediaRepo.deleteAll(...), now just delete the
        // post.
        postRepo.delete(post);
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
            flag=true;
        }
        like.setLikeType(likeType);
        like.setLikedAt(LocalDateTime.now());
        if(flag)
            post.getLikes().add(like); // add to post's likes collection

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
        //     parent.getReplies().add(newComment);
        //     postCommentRepo.save(parent);
        // }

        // 5) Save only the new comment. JPA will cascade properly (if you’ve set
        // cascade on PostComment).
        newComment = postCommentRepo.save(newComment);

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
        // not calling save(post) a second time because Hibernate will flush on
        // transaction commit.
        // But calling save(post) explicitly here is fine and ensures changes are queued
        // up.
        postRepo.save(post);
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

        if (originalParentExist) {
            Post originalParentPost = postRepo.findById(originalPost.getParentPostId())
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

        return sharedPost.getId();
    }

}
