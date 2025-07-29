// package StellarScholar.Private_Message;

// public interface WebSocketMessageBrokerConfigurer {

// }
// ChatWebSocketController.java

package com.example.demo.Private_Message;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageService messageService; // ✅ inject the service

    @MessageMapping("/chat")
    public void processMessage(@Payload MessageModel message) {
    // Optional: set timestamp if not coming from frontend
    if (message.getTimestamp() == null) {
        message.setTimestamp(LocalDate.now());
    }

    // Save the message to the database
    messageService.SendMessage(message);  // ✅ persist to DB

    // Send the message to the intended recipient
    messagingTemplate.convertAndSendToUser(
        message.getReceiverId().toString(),
        "/queue/messages",
        message
    );
}

}
