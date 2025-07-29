package com.example.demo.AI_Chatbot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryResponse {
    
    private List<ChatHistoryItem> chatHistory;
    private int totalCount;
    private boolean success;
    private String message;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatHistoryItem {
        private Long id;
        private String question;
        private String response;
        private LocalDateTime createdAt;
    }
    
    public static ChatHistoryResponse success(List<ChatHistoryItem> history, int total) {
        ChatHistoryResponse response = new ChatHistoryResponse();
        response.setChatHistory(history);
        response.setTotalCount(total);
        response.setSuccess(true);
        response.setMessage("Chat history retrieved successfully");
        return response;
    }
    
    public static ChatHistoryResponse error(String message) {
        ChatHistoryResponse response = new ChatHistoryResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
