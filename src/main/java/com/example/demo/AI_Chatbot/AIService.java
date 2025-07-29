package com.example.demo.AI_Chatbot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AIService {
    
    private final AIRepository aiRepository;
    
    /**
     * Process a chat request and generate AI response
     * @param request The chat request containing user ID and question
     * @return ChatResponse with the AI-generated response
     */
    public ChatResponse processChat(ChatRequest request) {
        try {
            log.info("Processing chat request for user: {} with question: {}", 
                    request.getUserId(), request.getQuestion());
            
            // Get recent chat history for context (last 5-10 messages)
            List<AIModel> recentHistory = getRecentChatHistory(request.getUserId(), 10);
            
            // Generate AI response based on question and context
            String aiResponse = generateAIResponse(request.getQuestion(), recentHistory);
            
            // Save the conversation to database
            AIModel chatEntry = AIModel.builder()
                    .userId(request.getUserId())
                    .question(request.getQuestion())
                    .response(aiResponse)
                    .build();
            
            AIModel savedEntry = aiRepository.save(chatEntry);
            
            log.info("Chat processed successfully for user: {}", request.getUserId());
            return ChatResponse.success(savedEntry);
            
        } catch (Exception e) {
            log.error("Error processing chat for user: {}", request.getUserId(), e);
            return ChatResponse.error("Failed to process chat request: " + e.getMessage());
        }
    }
    
    /**
     * Get chat history for a user
     * @param userId The user ID
     * @param limit Maximum number of entries to return
     * @return ChatHistoryResponse containing the chat history
     */
    @Transactional(readOnly = true)
    public ChatHistoryResponse getChatHistory(Long userId, int limit) {
        try {
            log.info("Retrieving chat history for user: {} with limit: {}", userId, limit);
            
            Pageable pageable = PageRequest.of(0, limit);
            List<AIModel> chatHistory = aiRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
            
            List<ChatHistoryResponse.ChatHistoryItem> historyItems = chatHistory.stream()
                    .map(this::convertToHistoryItem)
                    .collect(Collectors.toList());
            
            long totalCount = aiRepository.countByUserId(userId);
            
            return ChatHistoryResponse.success(historyItems, (int) totalCount);
            
        } catch (Exception e) {
            log.error("Error retrieving chat history for user: {}", userId, e);
            return ChatHistoryResponse.error("Failed to retrieve chat history: " + e.getMessage());
        }
    }
    
    /**
     * Get recent chat history for context
     * @param userId The user ID
     * @param limit Maximum number of entries
     * @return List of recent chat entries
     */
    private List<AIModel> getRecentChatHistory(Long userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return aiRepository.findLatestChatHistoryByUserId(userId, pageable);
    }
    
    /**
     * Generate AI response based on question and context
     * This is a simplified AI simulation. In a real implementation, 
     * you would integrate with actual AI services like OpenAI, Google AI, etc.
     * @param question The user's question
     * @param context Recent chat history for context
     * @return Generated AI response
     */
    private String generateAIResponse(String question, List<AIModel> context) {
        log.debug("Generating AI response for question: {}", question);
        
        // Build context from recent conversations
        StringBuilder contextBuilder = new StringBuilder();
        if (!context.isEmpty()) {
            contextBuilder.append("Previous conversation context:\n");
            for (AIModel entry : context) {
                contextBuilder.append("Q: ").append(entry.getQuestion()).append("\n");
                contextBuilder.append("A: ").append(entry.getResponse()).append("\n\n");
            }
        }
        
        // Simple AI simulation logic (replace with actual AI integration)
        String response = simulateAIResponse(question, contextBuilder.toString());
        
        log.debug("Generated AI response: {}", response);
        return response;
    }
    
    /**
     * Simulate AI response generation
     * In a real implementation, replace this with actual AI service calls
     * @param question The user's question
     * @param context Previous conversation context
     * @return Simulated AI response
     */
    private String simulateAIResponse(String question, String context) {
        String lowerQuestion = question.toLowerCase().trim();
        
        // Context-aware responses
        if (!context.isEmpty() && (lowerQuestion.contains("what") || lowerQuestion.contains("explain"))) {
            return "Based on our previous conversation, " + getContextualResponse(lowerQuestion);
        }
        
        // Greeting responses
        if (lowerQuestion.matches(".*(hello|hi|hey|good morning|good afternoon|good evening).*")) {
            return "Hello! I'm here to help you with any questions you might have. How can I assist you today?";
        }
        
        // Business-related questions
        if (lowerQuestion.contains("business") || lowerQuestion.contains("company") || lowerQuestion.contains("startup")) {
            return "That's a great business question! Based on current market trends and best practices, I'd recommend focusing on understanding your target audience, validating your business model, and building strong customer relationships. Would you like me to elaborate on any specific aspect?";
        }
        
        // Technology questions
        if (lowerQuestion.contains("technology") || lowerQuestion.contains("tech") || lowerQuestion.contains("software") || lowerQuestion.contains("programming")) {
            return "Technology is rapidly evolving! For software development, I'd suggest focusing on scalable architectures, clean code practices, and staying updated with industry standards. Are you working on a specific technology stack or project?";
        }
        
        // How-to questions
        if (lowerQuestion.startsWith("how")) {
            return "That's an excellent question! Let me break this down into actionable steps: 1) First, understand the fundamentals, 2) Research best practices, 3) Start with a simple implementation, 4) Iterate and improve. Would you like me to provide more specific guidance?";
        }
        
        // What questions
        if (lowerQuestion.startsWith("what")) {
            return "Great question! This topic involves several key concepts that are important to understand. Let me explain the main points and provide some context that might be helpful for your situation.";
        }
        
        // Why questions
        if (lowerQuestion.startsWith("why")) {
            return "That's a thoughtful question that gets to the heart of the matter. There are several reasons behind this, including practical considerations, industry standards, and user experience factors. Let me explain the key rationale.";
        }
        
        // Help or assistance requests
        if (lowerQuestion.contains("help") || lowerQuestion.contains("assist") || lowerQuestion.contains("support")) {
            return "I'm here to help! I can assist you with a wide range of topics including business advice, technology guidance, general information, and problem-solving. What specific area would you like help with?";
        }
        
        // Thank you responses
        if (lowerQuestion.matches(".*(thank|thanks|appreciate).*")) {
            return "You're very welcome! I'm glad I could help. If you have any more questions or need further assistance, please don't hesitate to ask.";
        }
        
        // Default intelligent response
        return String.format("Thank you for your question about '%s'. This is an interesting topic that deserves a thoughtful response. " +
                "Based on current knowledge and best practices, I'd recommend considering multiple perspectives and approaches. " +
                "Could you provide a bit more context about what specific aspect you're most interested in? " +
                "This would help me give you a more targeted and useful response.", 
                question.length() > 100 ? question.substring(0, 100) + "..." : question);
    }
    
    /**
     * Generate contextual responses based on conversation history
     * @param question The current question
     * @return Contextual response
     */
    private String getContextualResponse(String question) {
        if (question.contains("business")) {
            return "I can see we've been discussing business topics. Building on our previous conversation, it's important to focus on market research, customer validation, and sustainable growth strategies.";
        } else if (question.contains("technology") || question.contains("tech")) {
            return "Continuing our tech discussion, remember that choosing the right technology stack depends on your specific requirements, team expertise, and long-term maintenance considerations.";
        } else {
            return "building on what we discussed earlier, here are some additional insights that might be helpful for your situation.";
        }
    }
    
    /**
     * Convert AIModel to ChatHistoryItem
     * @param aiModel The AI model entity
     * @return ChatHistoryItem DTO
     */
    private ChatHistoryResponse.ChatHistoryItem convertToHistoryItem(AIModel aiModel) {
        return new ChatHistoryResponse.ChatHistoryItem(
                aiModel.getId(),
                aiModel.getQuestion(),
                aiModel.getResponse(),
                aiModel.getCreatedAt()
        );
    }
}
