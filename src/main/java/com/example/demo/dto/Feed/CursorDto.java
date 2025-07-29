package com.example.demo.dto.Feed;


/**
 * A “cursor” for paginated feed requests, containing:
 *  - rankScore: the score of the last item in the previous page (epoch ms)
 *  - postId:    the ID of that last item, used as a tie-breaker if two posts share the same score
 */
public class CursorDto {
    private double rankScore;
    private Long postId;
    private String lastDateTime;

    // Default no-arg constructor (needed for JSON deserialization)
    public CursorDto() { }

    public CursorDto(double rankScore, Long postId,String lastDateTime) {
        this.rankScore = rankScore;
        this.postId    = postId;
        this.lastDateTime=lastDateTime;
    }

    // ————— Getters & Setters —————
    public double getRankScore() { return rankScore; }
    public void setRankScore(Long rankScore) { this.rankScore = rankScore; }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public String getLastDateTime()
    {
        return lastDateTime;
    }

    void setLastDateTime(String lastDateTime)
    {
        this.lastDateTime=lastDateTime;
    }

    
}
