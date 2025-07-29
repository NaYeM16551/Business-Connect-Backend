package com.example.demo.model.Follow_Unfollow;
import com.example.demo.model.User;


import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(
    name = "user_follows",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_follower_followee",
        columnNames = { "follower_id", "followee_id" }
    ),
    indexes = {
        @Index(name = "idx_follower_id", columnList = "follower_id"),
        @Index(name = "idx_followee_id", columnList = "followee_id")
    }
)
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who did the “following”
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    // The user who is being followed
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followee_id", nullable = false)
    private User followee;

    // When the follow happened 
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Default constructor for JPA
    protected Follow() { }

    // Constructor for easy creation
    public Follow(User follower, User followee) {
        if (follower == null || followee == null) {
            throw new IllegalArgumentException("Follower and followee must be provided");
        }
        if (follower.getId().equals(followee.getId())) {
            throw new IllegalArgumentException("User cannot follow themselves");
        }
        this.follower = follower;
        this.followee = followee;
        this.createdAt = LocalDateTime.now();
    }

    // ─── Getters & Setters ─────────────────────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public User getFollower() {
        return follower;
    }

    public void setFollower(User follower) {
        this.follower = follower;
    }

    public User getFollowee() {
        return followee;
    }

    public void setFollowee(User followee) {
        this.followee = followee;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // No setter for createdAt since it’s set in constructor and should be immutable
}
