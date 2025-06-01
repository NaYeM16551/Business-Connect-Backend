package com.example.demo.service;

import com.example.demo.dto.Posts.CommentResponseDto;
import com.example.demo.dto.Posts.PostDto;
import com.example.demo.model.User;
import com.example.demo.model.Posts.Post;
import com.example.demo.model.Posts.PostComment;
import com.example.demo.model.Posts.PostLike;
import com.example.demo.model.Posts.PostMedia;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.Posts.PostMediaRepository;
import com.example.demo.repository.Posts.PostLikeRepository;
import com.example.demo.repository.Posts.PostCommentRepository;
import com.example.demo.repository.Posts.PostRepository;

import com.example.demo.service.Posts.PostService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * This integration test spins up the full Spring context (but overrides
 * DataSource
 * to use H2 in-memory) and allows us to call PostService.createPost(...) _and_
 * then verify data persisted via PostRepository, PostMediaRepository, etc.
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
class PostServiceIntegrationTest {

    @TestConfiguration
    static class CloudinaryTestConfig {
        @Bean
        public CloudinaryService cloudinaryService() {
            return Mockito.mock(CloudinaryService.class);
        }
    }

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostMediaRepository postMediaRepository;

    @Autowired
    private EntityManager em; // For flushing/clearing the persistence context

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private PostCommentRepository postCommentRepository;

    /**
     * We mock CloudinaryService so that no real network calls are made.
     * In production, CloudinaryService.uploadFile(...) would actually contact
     * Cloudinary.
     * Here, we simply return a dummy URL.
     */
    @Autowired
    private CloudinaryService cloudinaryService;

    private User savedUser;

    @BeforeEach
    void setUp() throws IOException {
        // 1) Insert a test User into the H2 in-memory DB.
        User user = new User();
        user.setUsername("integrationUser");
        user.setEmail("int@test.com");
        user.setPassword("irrelevant"); // satisfy any @NotBlank validation
        user.setIndustry(List.of("Technology", "Finance"));
        user.setInterests(List.of("Machine Learning", "Blockchain"));
        user.setAchievements(List.of("Published a research paper", "Speaker at TechConf 2024"));
        // (Add any other required fields on User entity)
        savedUser = userRepository.save(user);

        // 2) Stub CloudinaryService.uploadFile(...) to avoid actual upload:
        // Stub the mocked CloudinaryService once
        when(cloudinaryService.uploadFile(any()))
                .thenReturn("https://dummy.cloudinary.com/fake-image-url.jpg");
    }

    /**
     * Test createPost(...) method in PostService:
     * - We pass content + one MockMultipartFile + a Principal whose name == userId.
     * - We verify:
     * 1) PostService returns a non-null postId.
     * 2) That Post actually exists in the database.
     * 3) That exactly one PostMedia row was created with our dummy URL.
     */
    @Test
    void createPost_savesPostAndMedia() throws IOException {
        // ARRANGE
        String content = "Hello, this is a test post!";
        // Create a fake image file (300 bytes of dummy data)
        MockMultipartFile dummyFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "dummy-data-123".getBytes());

        MockMultipartFile dummyFile2 = new MockMultipartFile(
                "file2",
                "test-image2.jpg",
                "image/jpeg",
                "dummy-data-456".getBytes());

        // Create a Principal where getName() returns our savedUser.getId().toString()
        Principal fakePrincipal = () -> String.valueOf(savedUser.getId());

        // ACT
        Long newPostId = postService.createPost(
                content,
                new MockMultipartFile[] { dummyFile, dummyFile2 },
                fakePrincipal);
        assertThat(newPostId).isNotNull();

        // ASSERT: Verify the Post was persisted
        Optional<Post> maybePost = postRepository.findById(newPostId);
        assertThat(maybePost).isPresent();
        Post persistedPost = maybePost.get();
        assertThat(persistedPost.getContent()).isEqualTo(content);
        // The Post’s user ID should equal the savedUser’s ID
        assertThat(persistedPost.getUser().getId()).isEqualTo(savedUser.getId());

        // ASSERT: Verify exactly one media row exists for this post, with our dummy URL
        List<PostMedia> mediaList = postMediaRepository.findAllByPostId(newPostId);
        assertThat(mediaList).hasSize(2);

        PostMedia persistedMedia = mediaList.get(0);
        assertThat(persistedMedia.getMediaUrl())
                .isEqualTo("https://dummy.cloudinary.com/fake-image-url.jpg");
        assertThat(persistedMedia.getMediaType()).isEqualTo("image/jpeg");

        // ASSERT: Ensure CloudinaryService.uploadFile(...) was called exactly once
        verify(cloudinaryService, times(2)).uploadFile(any());
    }

    /*
     * Test getPostsByPostId(...) method:
     * - First we insert a Post + no media manually via repository.
     * - Then call postService.getPostsByPostId(postId) and verify the returned DTO.
     */
    @Test
    void getPostsByPostId_returnsCorrectDto() {
        // ARRANGE: Manually save a Post (without media) in H2
        Post manualPost = new Post();
        manualPost.setContent("Manually inserted post");
        manualPost.setCreatedAt(java.time.LocalDateTime.now());
        manualPost.setUser(savedUser);
        // parentPostId & shareCount left null/zero
        manualPost = postRepository.save(manualPost);

        Long manualPostId = manualPost.getId();

        // ACT
        var dto = postService.getPostsByPostId(manualPostId);

        // ASSERT
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(manualPostId);
        assertThat(dto.getContent()).isEqualTo("Manually inserted post");
        // Because we didn’t add media, dto.getMedia() should be an empty list
        assertThat(dto.getMedia()).isEmpty();
    }

    @Test
    void deletePost_removesPostAndMediaAndChecksOwnership() throws Exception {
        // ARRANGE
        Post post = new Post();
        post.setContent("To be deleted");
        post.setCreatedAt(java.time.LocalDateTime.now());
        post.setUser(savedUser);
        post = postRepository.save(post);

        PostMedia m1 = new PostMedia();
        m1.setMediaUrl("url-1");
        m1.setMediaType("image/png");
        m1.setPost(post);
        post.getMedia().add(m1);

        PostMedia m2 = new PostMedia();
        m2.setMediaUrl("url-2");
        m2.setMediaType("image/png");
        m2.setPost(post);
        post.getMedia().add(m2);

        post = postRepository.save(post); // cascade saves media

        Long postIdToDelete = post.getId();

        // Verify we have 2 media entries in the DB
        assertThat(postMediaRepository.findAllByPostId(postIdToDelete)).hasSize(2);

        // ACT: Delete as the correct user
        postService.deletePost(postIdToDelete, savedUser.getId());

        // Optionally flush/clear
        em.flush();
        em.clear();

        // ASSERT: Verify post is gone
        Optional<Post> deletedPost = postRepository.findById(postIdToDelete);
        assertThat(deletedPost).isEmpty();

        // Verify all media rows were removed
        assertThat(postMediaRepository.findAllByPostId(postIdToDelete)).isEmpty();

        // Negative test: Deleting with a different user should throw
        Post otherPost = new Post();
        otherPost.setContent("Another post");
        otherPost.setCreatedAt(java.time.LocalDateTime.now());
        otherPost.setUser(savedUser);
        otherPost = postRepository.save(otherPost);

        Long otherPostId = otherPost.getId();
        Long bogusUserId = savedUser.getId() + 999L;

        assertThatThrownBy(() -> postService.deletePost(otherPostId, bogusUserId)).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("You can only delete your own posts");
    }

    @Test
    void getPostsByUserId_returnsAllUserPostsAsDto() {
        // Arrange: Create and save a user

        // Create posts for this user
        Post post1 = new Post();
        post1.setContent("First");
        post1.setCreatedAt(LocalDateTime.now());
        post1.setUser(savedUser);

        Post post2 = new Post();
        post2.setContent("Second");
        post2.setCreatedAt(LocalDateTime.now());
        post2.setUser(savedUser);

        post1 = postRepository.save(post1);
        post2 = postRepository.save(post2);

        // Act
        List<PostDto> dtos = postService.getPostsByUserId(savedUser.getId());

        // Assert
        assertThat(dtos).hasSize(2);
        assertThat(dtos).extracting("content").containsExactlyInAnyOrder("First", "Second");
    }

    @Test
    void likePost_createsAndUpdatesLike() {
        // Arrange: User and post
        // User user = userRepository.save(new User("bob", "bob@test.com",
        // "irrelevant"));
        Post post = new Post();
        post.setContent("Like me!");
        post.setUser(savedUser);
        post = postRepository.save(post);

        // Act: First like
        postService.likePost(post.getId(), savedUser.getId(), 1); // likeType=1 (like)

        // Assert: Like is present
        PostLike like = postLikeRepository.findByPostIdAndUserId(post.getId(), savedUser.getId()).orElse(null);
        assertThat(like).isNotNull();
        assertThat(like.getLikeType()).isEqualTo(1);

        // Act: Update like type
        postService.likePost(post.getId(), savedUser.getId(), 2); // likeType=2 (love)
        PostLike updated = postLikeRepository.findByPostIdAndUserId(post.getId(), savedUser.getId()).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getLikeType()).isEqualTo(2);
    }

    @Test
    void commentOnPost_createsComment() {
        // Arrange
        User user = savedUser; // Use the saved user from setUp()
        Post post = new Post();
        post.setContent("Hello comments");
        post.setUser(user);
        post = postRepository.save(post);

        // Act
        Long commentId = postService.commentOnPost(post.getId(), user.getId(), "Nice post!", -1L);

        // Assert
        PostComment comment = postCommentRepository.findById(commentId).orElse(null);
        assertThat(comment).isNotNull();
        assertThat(comment.getContent()).isEqualTo("Nice post!");
        assertThat(comment.getPost().getId()).isEqualTo(post.getId());
        assertThat(comment.getUser().getId()).isEqualTo(user.getId());
        assertThat(null == comment.getParentComment()).isTrue(); // No parent comment
    }

    @Test
    void getCommentsByPostId_returnsAllComments() {
        // Arrange
        User user = savedUser;
        Post post = new Post();
        post.setContent("Discussion");
        post.setUser(user);
        post = postRepository.save(post);

        // Add a top-level comment
        Long comment1Id = postService.commentOnPost(post.getId(), user.getId(), "First!", -1L);
        // Optionally flush/clear

        // Add a reply to that comment
        postService.commentOnPost(post.getId(), user.getId(), "Reply!", comment1Id);

        // Optionally flush/clear
        em.flush();
        em.clear();

        // Act
        List<CommentResponseDto> comments = postService.getCommentsByPostId(post.getId());

        // Assert
        assertThat(comments).hasSize(1); // One top-level comment
        assertThat(comments.get(0).getReplies()).hasSize(1); // One reply
        assertThat(comments.get(0).getReplies().get(0).getContent()).isEqualTo("Reply!");
    }

    @Test
    void editComment_updatesCommentContent() {
        // Arrange
        User user = savedUser;
        Post post = new Post();
        post.setContent("Edit comment post");
        post.setUser(user);
        post = postRepository.save(post);

        Long commentId = postService.commentOnPost(post.getId(), user.getId(), "Before edit", -1L);

        // Act
        postService.editComment(post.getId(), commentId, user.getId(), "After edit");

        // Assert
        PostComment updated = postCommentRepository.findById(commentId).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getContent()).isEqualTo("After edit");
    }

    @Test
    void deleteComment_removesComment() {
        // Arrange
        User user = savedUser;
        Post post = new Post();
        post.setContent("Delete comment post");
        post.setUser(user);
        post = postRepository.save(post);

        Long commentId = postService.commentOnPost(post.getId(), user.getId(), "To delete", -1L);

        // Act
        postService.deleteComment(post.getId(), commentId, user.getId());

        // Assert
        assertThat(postCommentRepository.findById(commentId)).isEmpty();
    }

    @Test
    void sharePost_createsSharedPostAndIncrementsShareCount() {
        // Arrange
        User user = savedUser;
        Post post = new Post();
        post.setContent("Share me");
        post.setUser(user);
        post = postRepository.save(post);

        long before = post.getShareCount();

        // Act
        Long sharedPostId = postService.sharePost(post.getId(), user.getId(), "Check this out!");

        // Assert: shared post exists
        Post shared = postRepository.findById(sharedPostId).orElse(null);
        assertThat(shared).isNotNull();
        assertThat(shared.getParentPostId()).isEqualTo(post.getId());
        assertThat(shared.getContent()).isEqualTo("Check this out!");

        // Assert: original post share count incremented
        Post updatedOriginal = postRepository.findById(post.getId()).orElse(null);
        assertThat(updatedOriginal.getShareCount()).isEqualTo(before + 1);
    }

}
