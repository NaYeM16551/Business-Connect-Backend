
package com.example.demo.dto.Posts;

import java.time.LocalDateTime;
import java.util.List;

public class CommentResponseDto {
    private String authorName;
    private LocalDateTime commentedAt;
    private String content;
    private Long id;
    private Long parentCommentId;
    private List<CommentResponseDto> replies;


    // Constructors
    public CommentResponseDto() {}

    public CommentResponseDto(String authorName, LocalDateTime commentedAt, String content, Long id, Long parentCommentId, List<CommentResponseDto> replies) {
        this.authorName = authorName;
        this.commentedAt = commentedAt;
        this.content = content;
        this.id = id;
        this.parentCommentId = parentCommentId;
        this.replies = replies;
    }

    // Getters and setters
    public String getAuthorName() {
        return authorName;
    }
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    public LocalDateTime getCommentedAt() {
        return commentedAt;
    }
    public void setCommentedAt(LocalDateTime commentedAt) {
        this.commentedAt = commentedAt;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getParentCommentId() {
        return parentCommentId;
    }
    public void setParentCommentId(Long parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public List<CommentResponseDto> getReplies() {
        return replies;
    }
    public void setReplies(List<CommentResponseDto> replies) {
        this.replies = replies;
    }

}
