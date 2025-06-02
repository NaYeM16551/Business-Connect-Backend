package com.example.demo.dto.Feed;

import java.util.List;

/**
 * A single feed item returned to the client.
 */
public class FeedItemDto {
    private Long postId;
    private Long authorId;
    private String authorName;
    private String authorAvatarUrl;
    private String contentSnippet;
    private List<String> mediaUrls;
    private String createdAt;   // ISO‐8601 string
    private Long likeCount;
    private Long commentCount;
    private Long shareCount;
    private Double rankScore;   // the Redis ZSET score
    private Long myLikeType;
    private boolean IsSharedByMe;

    // ————— Getters & Setters —————
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorAvatarUrl() { return authorAvatarUrl; }
    public void setAuthorAvatarUrl(String authorAvatarUrl) { this.authorAvatarUrl = authorAvatarUrl; }

    public String getContentSnippet() { return contentSnippet; }
    public void setContentSnippet(String contentSnippet) { this.contentSnippet = contentSnippet; }

    public List<String> getMediaUrls() { return mediaUrls; }
    public void setMediaUrls(List<String> mediaUrls) { this.mediaUrls = mediaUrls; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public Long getLikeCount() { return likeCount; }
    public void setLikeCount(Long likeCount) { this.likeCount = likeCount; }

    public Long getCommentCount() { return commentCount; }
    public void setCommentCount(Long commentCount) { this.commentCount = commentCount; }

    public Long getShareCount() { return shareCount; }
    public void setShareCount(Long shareCount) { this.shareCount = shareCount; }

    public Double getRankScore() { return rankScore; }
    public void setRankScore(Double rankScore) { this.rankScore = rankScore; }

    public Long getMyLikeType() { return myLikeType; }
    public void setMyLikeType(Long myLikeType) { this.myLikeType = myLikeType; }

    public boolean getIsSharedByMe() { return IsSharedByMe; }
    public void setIsSharedByMe(boolean sharedByMe) { IsSharedByMe = sharedByMe; }



}
