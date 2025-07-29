package com.example.demo.AI_Chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    
    private Long id;
    private String question;
    private String response;
    private LocalDateTime createdAt;
    private boolean success;
    private String message;
    
    public static ChatResponse success(AIModel chatHistory) {
        return ChatResponse.builder()
                .id(chatHistory.getId())
                .question(chatHistory.getQuestion())
                .response(chatHistory.getResponse())
                .createdAt(chatHistory.getCreatedAt())
                .success(true)
                .message("Chat response generated successfully")
                .build();
    }
    
    public static ChatResponse error(String message) {
        return ChatResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
