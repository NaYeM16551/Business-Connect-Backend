package com.example.demo.dto.Feed;
/**
 * A “cursor” for paginated feed requests, containing:
 *  - rankScore: the score of the last item in the previous page (epoch ms)
 *  - postId:    the ID of that last item, used as a tie-breaker if two posts share the same score
 */
public class CursorDto {
    private double rankScore;
    private Long postId;

    // Default no-arg constructor (needed for JSON deserialization)
    public CursorDto() { }

    public CursorDto(double rankScore, Long postId) {
        this.rankScore = rankScore;
        this.postId    = postId;
    }

    // ————— Getters & Setters —————
    public double getRankScore() { return rankScore; }
    public void setRankScore(Long rankScore) { this.rankScore = rankScore; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
}
