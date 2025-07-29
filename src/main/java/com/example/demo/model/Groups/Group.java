package com.example.demo.model.Groups;

import jakarta.persistence.*;
import java.time.Instant;

import com.example.demo.model.User;

@Entity
@Table(name = "groups")
public class Group {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_id")
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;



    private String type;

    @Column(columnDefinition = "TEXT")
    private String description;

    public enum Privacy { PUBLIC, CLOSED, PRIVATE }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Privacy privacy = Privacy.PUBLIC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "cover_image")
    private String coverImage;

    @Column(name = "member_count", nullable = false)
    private Integer memberCount = 0;

    @Column(name = "post_count", nullable = false)
    private Integer postCount = 0;

    // ====== getters & setters ======
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Privacy getPrivacy() {
        return privacy;
    }
    public void setPrivacy(Privacy privacy) {
        this.privacy = privacy;
    }
    public User getOwner() {
        return owner;
    }
    public void setOwner(User owner) {
        this.owner = owner;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
    public String getCoverImage() {
        return coverImage;
    }
    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }
    public Integer getMemberCount() {
        return memberCount;
    }
    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }
    public Integer getPostCount() {
        return postCount;
    }
    public void setPostCount(Integer postCount) {
        this.postCount = postCount;
    }

    public void setType(String type)
    {
        this.type=type;
    }

    public String getType()
    {
        return this.type;
    }
    
}