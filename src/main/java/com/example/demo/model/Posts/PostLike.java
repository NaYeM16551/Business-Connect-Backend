package com.example.demo.model.Posts;

import java.time.LocalDateTime;

import com.example.demo.model.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "post_likes", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "post_id" }))
public class PostLike {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    private LocalDateTime likedAt;

    private int likeType;//0 for no-react,,1--->love,2-->like,3--->wow,4--->angry,5--->haha
    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public LocalDateTime getLikedAt() {
        return likedAt;
    }

    public void setLikedAt(LocalDateTime likedAt) {
        this.likedAt = likedAt;
    }

    public void setLikeType(int likeType) {
        this.likeType = likeType;
    }

    public int getLikeType()
    {
        return this.likeType;
    }
}
