package com.example.demo.dto.Posts;
import java.util.List;
import java.time.LocalDateTime;

public class PostDto {
    public Long id;
    public String content;
    public List<PostMediaDto> media;
    LocalDateTime createdAt;
    public Long likeCount = 0L; // Default value for like count
    public Long shareCount = 0L; // Default value for share count
    public Long commentCount = 0L; // Default value for comment count

    //write getter setter
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public List<PostMediaDto> getMedia() {
        return media;
    }
    public void setMedia(List<PostMediaDto> media) {
        this.media = media;
    }

    //getter and setter for createdAt
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getLikeCount() {
        return likeCount;
    }
    public void setLikeCount(Long likeCount) {
        this.likeCount = likeCount;
    }
    public Long getShareCount() {
        return shareCount;
    }
    public void setShareCount(Long shareCount) {
        this.shareCount = shareCount;
    }
    public Long getCommentCount() {
        return commentCount;
    }
    public void setCommentCount(Long commentCount) {
        this.commentCount = commentCount;
    }

}
