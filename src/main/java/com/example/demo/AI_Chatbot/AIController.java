package com.example.demo.AI_Chatbot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@RestController
@RequestMapping("/api/v1/{id}")
@RequiredArgsConstructor
@Slf4j
@Validated
@CrossOrigin(origins = "*") // Configure as needed for your frontend
public class AIController {

    private final AIService aiService;

    /**
     * Process a chat message and get AI response
     * @param request The chat request containing user ID and question
     * @return ChatResponse with AI-generated response
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> processChat(@Valid @RequestBody ChatRequest request) {
        log.info("Received chat request from user: {}", request.getUserId());
        
        try {
            ChatResponse response = aiService.processChat(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error processing chat request", e);
            ChatResponse errorResponse = ChatResponse.error("An unexpected error occurred while processing your request");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get chat history for a specific user
     * @param userId The user ID
     * @param limit Maximum number of chat entries to return (default: 20, max: 100)
     * @return ChatHistoryResponse containing the chat history
     */
    @GetMapping("/history/{userId}")
    public ResponseEntity<ChatHistoryResponse> getChatHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
        
        log.info("Retrieving chat history for user: {} with limit: {}", userId, limit);
        
        try {
            ChatHistoryResponse response = aiService.getChatHistory(userId, limit);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error retrieving chat history for user: {}", userId, e);
            ChatHistoryResponse errorResponse = ChatHistoryResponse.error("Failed to retrieve chat history");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Health check endpoint for the AI chatbot service
     * @return Simple health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("AI Chatbot service is running");
    }

    /**
     * Get recent chat context for a user (useful for debugging or frontend preview)
     * @param userId The user ID
     * @return Recent chat entries (last 5)
     */
    @GetMapping("/recent/{userId}")
    public ResponseEntity<ChatHistoryResponse> getRecentChats(@PathVariable Long userId) {
        log.info("Retrieving recent chats for user: {}", userId);
        
        try {
            ChatHistoryResponse response = aiService.getChatHistory(userId, 5);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
            
        } catch (Exception e) {
            log.error("Error retrieving recent chats for user: {}", userId, e);
            ChatHistoryResponse errorResponse = ChatHistoryResponse.error("Failed to retrieve recent chats");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
