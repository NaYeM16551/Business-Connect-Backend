package com.example.demo.dto.Feed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * A page of feed items, plus the “next cursor” for further pages.
 */
public class FeedPageResponseDto {
    private List<FeedItemDto> items;
    private CursorDto nextCursor;

    public FeedPageResponseDto() { }

    public FeedPageResponseDto(List<FeedItemDto> items, CursorDto nextCursor) {
        this.items      = items;
        this.nextCursor = nextCursor;
    }

    // ————— Getters & Setters —————
    public List<FeedItemDto> getItems() { return items; }
    public void setItems(List<FeedItemDto> items) { this.items = items; }

    public CursorDto getNextCursor() { return nextCursor; }
    public void setNextCursor(CursorDto nextCursor) { this.nextCursor = nextCursor; }

    /**
     * Serialize this object to a JSON string (for caching in Redis).
     */
    public String toJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize FeedPageResponse to JSON", e);
        }
    }

    /**
     * Parse a JSON string (from Redis) back into a FeedPageResponse.
     */
    public static FeedPageResponseDto fromJson(String json) {
        try {
            return new ObjectMapper()
                    .readValue(json, FeedPageResponseDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize FeedPageResponse JSON", e);
        }
    }
}
