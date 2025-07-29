# AI Chatbot Feature

This package implements an AI chatbot functionality for the Business Connect application. The chatbot maintains conversation history and provides context-aware responses.

## Features

- **Context-Aware Responses**: Uses previous 5-10 conversation messages as context
- **Conversation History**: Stores all chat interactions in the database
- **User-Specific Chats**: Each user has their own isolated chat history
- **RESTful API**: Clean REST endpoints for frontend integration
- **Intelligent Response Simulation**: Smart response generation based on question patterns

## Architecture

### Components

1. **AIModel**: JPA entity representing chat history entries
2. **AIRepository**: Data access layer for chat operations
3. **AIService**: Business logic layer handling chat processing
4. **AIController**: REST API endpoints
5. **DTOs**: Request/Response objects for API communication

### Database Schema

```sql
CREATE TABLE ai_chat_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    question VARCHAR(2000) NOT NULL,
    response VARCHAR(5000) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

## API Endpoints

### 1. Process Chat Message
```
POST /api/v1/ai-chat/chat
Content-Type: application/json

{
    "userId": 123,
    "question": "What are the best practices for starting a business?"
}
```

**Response:**
```json
{
    "id": 1,
    "question": "What are the best practices for starting a business?",
    "response": "That's a great business question! Based on current market trends...",
    "createdAt": "2025-07-28T10:30:00",
    "success": true,
    "message": "Chat response generated successfully"
}
```

### 2. Get Chat History
```
GET /api/v1/ai-chat/history/{userId}?limit=20
```

**Response:**
```json
{
    "chatHistory": [
        {
            "id": 1,
            "question": "What are the best practices for starting a business?",
            "response": "That's a great business question! Based on current market trends...",
            "createdAt": "2025-07-28T10:30:00"
        }
    ],
    "totalCount": 1,
    "success": true,
    "message": "Chat history retrieved successfully"
}
```

### 3. Get Recent Chats
```
GET /api/v1/ai-chat/recent/{userId}
```

### 4. Health Check
```
GET /api/v1/ai-chat/health
```

## Usage Examples

### Frontend Integration

```javascript
// Send a chat message
const sendMessage = async (userId, question) => {
    const response = await fetch('/api/v1/ai-chat/chat', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({
            userId: userId,
            question: question
        })
    });
    
    return await response.json();
};

// Get chat history
const getChatHistory = async (userId, limit = 20) => {
    const response = await fetch(`/api/v1/ai-chat/history/${userId}?limit=${limit}`);
    return await response.json();
};
```

### Spring Boot Integration

```java
@Autowired
private AIService aiService;

public void exampleUsage() {
    // Create a chat request
    ChatRequest request = new ChatRequest(123L, "How do I start a business?");
    
    // Process the chat
    ChatResponse response = aiService.processChat(request);
    
    if (response.isSuccess()) {
        System.out.println("AI Response: " + response.getResponse());
    }
}
```

## Configuration

### Required Dependencies
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Validation
- Lombok
- PostgreSQL (or your preferred database)

### Database Configuration
Add the following to your `application.properties`:

```properties
# Database configuration (already exists in your project)
spring.datasource.url=jdbc:postgresql://localhost:5432/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

## Extending the AI Integration

The current implementation uses a simulated AI response system. To integrate with real AI services:

### 1. OpenAI Integration
```java
@Service
public class OpenAIService {
    
    @Value("${openai.api.key}")
    private String apiKey;
    
    public String generateResponse(String question, String context) {
        // Implement OpenAI API call
        // Use RestTemplate or WebClient to call OpenAI API
    }
}
```

### 2. Google AI Integration
```java
@Service
public class GoogleAIService {
    
    public String generateResponse(String question, String context) {
        // Implement Google AI API call
    }
}
```

### 3. Update AIService
```java
@Autowired
private OpenAIService openAIService; // or GoogleAIService

private String generateAIResponse(String question, List<AIModel> context) {
    String contextStr = buildContextString(context);
    return openAIService.generateResponse(question, contextStr);
}
```

## Testing

### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class AIServiceTest {
    
    @Mock
    private AIRepository aiRepository;
    
    @InjectMocks
    private AIService aiService;
    
    @Test
    void testProcessChat() {
        // Test implementation
    }
}
```

### Integration Tests
```java
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AIControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testChatEndpoint() {
        // Test implementation
    }
}
```

## Security Considerations

1. **Input Validation**: All user inputs are validated using Bean Validation
2. **SQL Injection Prevention**: Using JPA/Hibernate with parameterized queries
3. **Rate Limiting**: Consider implementing rate limiting for chat endpoints
4. **Authentication**: Integrate with your existing security system
5. **Data Privacy**: Ensure chat data is properly secured and follows privacy regulations

## Performance Optimization

1. **Database Indexing**: Add indexes on `user_id` and `created_at` columns
2. **Pagination**: Implement pagination for large chat histories
3. **Caching**: Consider Redis caching for frequently accessed chat histories
4. **Async Processing**: For heavy AI processing, consider async handling

## Monitoring and Logging

The service includes comprehensive logging at INFO, DEBUG, and ERROR levels:
- Chat request processing
- Response generation
- Error handling
- Performance metrics

## Future Enhancements

1. **Real AI Integration**: Replace simulation with actual AI services
2. **File Upload Support**: Allow users to upload documents for context
3. **Multi-language Support**: Support for multiple languages
4. **Chat Sessions**: Group related conversations into sessions
5. **Admin Dashboard**: Administrative interface for monitoring chats
6. **Analytics**: Chat usage analytics and insights
