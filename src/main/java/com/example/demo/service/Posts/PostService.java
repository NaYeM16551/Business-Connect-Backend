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

        Post post = new Post();
        post.setContent(content);
        post.setCreatedAt(java.time.LocalDateTime.now());

        // Set User (only set ID for reference)
        User user = new User();
        user.setId(userId);
        post.setUser(user);

        post = postRepo.save(post);

        for (MultipartFile file : files) {
            String url = cloudinaryService.uploadFile(file);
            System.out.println("Uploaded file URL: " + url);

            PostMedia media = new PostMedia();
            media.setMediaUrl(url);
            media.setMediaType(file.getContentType()); // <-- Set mediaType!
            media.setPost(post);

            mediaRepo.save(media);
        }

        return post.getId();
    }

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
        Post post = postRepo.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));

        post.getMedia().size();
        // Check if the post belongs to the user
        if (!post.getUser().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own posts");
        }

        // Delete media associated with the post
        mediaRepo.deleteAll(post.getMedia());

        // Delete the post itself
        postRepo.delete(post);
    }

    @Transactional
    public void likePost(Long postId, Long userId, Integer likeType) {
        Post post = postRepo.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));

        PostLike like = postLikeRepo.findByPostIdAndUserId(postId, userId).orElse(null);

        if (like == null) {
            like = new PostLike();
            like.setPost(post);
            like.setUser(userRepo.getReferenceById(userId)); // more robust than just new User().setId()
        }
        like.setLikeType(likeType);
        like.setLikedAt(LocalDateTime.now());
        postLikeRepo.save(like); // save directly

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
        if (parentCommentId != null) {
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
            // (Because of mappedBy="parentComment", parent's replies list will be updated
            // automatically
            // when we save the child. No need to call parent.getReplies().add(...).)
        }

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

        // 4) Delete the comment. Because of cascade & orphanRemoval on 'replies',
        // any replies (children) of this comment will be deleted automatically.
        postCommentRepo.delete(comment);
    }

}
