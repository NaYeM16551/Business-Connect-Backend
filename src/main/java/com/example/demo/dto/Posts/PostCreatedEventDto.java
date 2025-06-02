package com.example.demo.dto.Posts;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * A minimal “event” published whenever someone creates (or shares) a post.
 * Contains only the fields needed for fan‐out and initial feed indexing.
 */
public class PostCreatedEventDto implements Serializable {

    // The unique ID of the new post in PostgreSQL
    private Long postId;

    // The author’s user ID
    private Long authorId;

    // When the post was created (converted to Java Instant for UTC precision)
    private Instant createdAt;

    // A short snippet of the post’s text (we don’t need full content here)
    private String contentSnippet;

    // Any media URLs (images/videos) attached to the post
    private List<String> mediaUrls;

    // Default constructor (required for some serializers)
    public PostCreatedEventDto() { }

    /**
     * Construct a new event
     * @param postId         ID of the newly created post
     * @param authorId       ID of the user who created it
     * @param createdAt      UTC timestamp of creation
     * @param contentSnippet First ~200 characters of the post
     * @param mediaUrls      List of image/video URLs
     */
    public PostCreatedEventDto(Long postId,
                             Long authorId,
                             Instant createdAt,
                             String contentSnippet,
                             List<String> mediaUrls) {
        this.postId       = postId;
        this.authorId     = authorId;
        this.createdAt    = createdAt;
        this.contentSnippet = contentSnippet;
        this.mediaUrls    = mediaUrls;
    }

    // ————————— Getter & Setter methods —————————

    public Long getPostId() {
        return postId;
    }
    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getAuthorId() {
        return authorId;
    }
    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getContentSnippet() {
        return contentSnippet;
    }
    public void setContentSnippet(String contentSnippet) {
        this.contentSnippet = contentSnippet;
    }

    public List<String> getMediaUrls() {
        return mediaUrls;
    }
    public void setMediaUrls(List<String> mediaUrls) {
        this.mediaUrls = mediaUrls;
    }
}
