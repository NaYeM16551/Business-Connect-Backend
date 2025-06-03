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
    

    //parent info
    private Long parentPostId;  // if this is a share, the original post ID
    private String parentAuthorName; // if this is a share, the original author's name
    private String parentAuthorAvatarUrl; // if this is a share, the original author's avatar URL
    private Long parentAuthorId;
    private String parentPostContentSnippet;

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


    public Long getParentPostId() { return parentPostId; }
    public void setParentPostId(Long parentPostId) { this.parentPostId = parentPostId; }
    public String getParentAuthorName() { return parentAuthorName; }
    public void setParentAuthorName(String parentAuthorName) { this.parentAuthorName = parentAuthorName; }
    public String getParentAuthorAvatarUrl() { return parentAuthorAvatarUrl; }
    public void setParentAuthorAvatarUrl(String parentAuthorAvatarUrl) { this.parentAuthorAvatarUrl = parentAuthorAvatarUrl; }
    
    public Long getParentAuthorId() { return parentAuthorId; }
    public void setParentAuthorId(Long parentAuthorId) { this.parentAuthorId = parentAuthorId;}

    public String getParentPostContentSnippet() { return parentPostContentSnippet; }
    public void setParentPostContentSnippet(String parentPostContentSnippet) { this.parentPostContentSnippet = parentPostContentSnippet; }


}
