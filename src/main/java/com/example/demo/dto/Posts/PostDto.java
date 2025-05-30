package com.example.demo.dto.Posts;
import java.util.List;
import java.time.LocalDateTime;

public class PostDto {
    public Long id;
    public String content;
    public List<PostMediaDto> media;
    LocalDateTime createdAt;

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

}
