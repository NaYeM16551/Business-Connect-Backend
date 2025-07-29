
package com.example.demo.Private_Message;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import com.example.demo.exception.ResourceNotFoundException;//.Exception.ResourceNotFoundException;
import com.example.demo.model.User;

@RestController
@RequestMapping("/api/v1/{id}")
public class MessageController {
    @Autowired
    private MessageService messageService;

    @PostMapping("/messages/SendMessage")
    public ResponseEntity<MessageModel> createMessage(@RequestBody MessageModel message) {
        MessageModel createdMessage = messageService.createMessage(message);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdMessage);
    }

    // @GetMapping("/messages/{R_Id}")
    // public ResponseEntity<MessageModel> getMessageById(@PathVariable Long R_Id) {
    //     try {
    //         MessageModel message = messageService.getMessageById(R_Id);
    //         return ResponseEntity.ok(message);
    //     } catch (ResourceNotFoundException e) {
    //         return ResponseEntity.notFound().build();
    //     }
    // }

    @DeleteMapping("/messages/delete/{R_Id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long R_Id) {
        try {
            messageService.deleteMessage(R_Id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/messages/chat/{senderId}")
    public ResponseEntity<List<MessageModel>> showSingleChat(@PathVariable Long senderId, @PathVariable Long id) {
        System.out.println("Fetching chat messages for sender ID: " + senderId + " and receiver ID: " + id);
        List<MessageModel> chatMessages = messageService.ShowASingleChat(senderId, id);
        return ResponseEntity.ok(chatMessages);
    }

    // @GetMapping("/messages/senders/{receiverId}")
    // public ResponseEntity<List<StudentModel>> showAllSenders(@PathVariable Long receiverId) {
    //     List<StudentModel> senders = messageService.ShowAllSendersByReceiverId(receiverId);
    //     return ResponseEntity.ok(senders);
    // }

    @DeleteMapping("/messages/conversation/{R_Id}")
    public ResponseEntity<Void> deleteAllMessagesByConversationId(@PathVariable Long R_Id) {
        messageService.deleteAllMessagesByConversationId(R_Id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/messages/chat/{senderId}/send")
    public ResponseEntity<Void> sendMessage(@RequestBody MessageModel message) {
        messageService.SendMessage(message);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/messages")
    public ResponseEntity<List<UserDTO>> getAllMessages(@PathVariable Long id) {
        System.out.println("Fetching all messages for receiver ID: " + id);
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        List<UserDTO> messages = messageService.ShowAllSendersByReceiverId(id);
        return ResponseEntity.ok(messages);
    }

    @PostMapping("/messages/chat/{senderId}/edit")
    public ResponseEntity<MessageModel> editMessage(@PathVariable Long senderId, @RequestBody MessageModel message) {
        // Assuming the message contains the ID of the message to be edited
        messageService.SendMessage(message);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @PostMapping("/messages/chat/{senderId}/FileSend")
    public ResponseEntity<Void> sendFile(@PathVariable Long senderId, @RequestBody MessageModel message) {
        // Assuming the message contains the file information
        messageService.SendMessage(message);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/messages/chat/{senderId}")
    public ResponseEntity<Void> MarkAsRead(@PathVariable Long senderId, @PathVariable Long id) {
        // Assuming the message contains the ID of the message to be marked as read
        //messageService.SendMessage(message);
        //return ResponseEntity.status(HttpStatus.CREATED).build(); 
        System.out.println("Marking message as read for sender ID: " + senderId + " and receiver ID: " + id);
        if (senderId == null || id == null) {
            return ResponseEntity.badRequest().build();
        }
        messageService.MarkAsRead(senderId, id);
        System.out.println("---------------------AFTER MARKING AS READ---------------------");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping("/messages/unread")
    public ResponseEntity<Long> getUnreadMessageCount(@PathVariable Long id) throws ResourceNotFoundException {
        // Assuming you have a method in MessageService to count unread messages
        Long unreadCount = messageService.GetUnreadMessageCount(id);
        return ResponseEntity.ok(unreadCount);
    }

}
