package com.example.demo.Private_Message;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.User;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private MessageUserRepository messageUserRepository;

    public MessageModel createMessage(MessageModel message) {
        return messageRepository.save(message);
    }

    public MessageModel getMessageById(Long messageId) throws ResourceNotFoundException {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found for this id :: " + messageId));
    }

    public void deleteMessage(Long messageId) throws ResourceNotFoundException {
        if (!messageRepository.existsById(messageId)) {
            throw new ResourceNotFoundException("Message not found for this id :: " + messageId);
        }
        messageRepository.deleteById(messageId);
    }
    
    public List<MessageModel> ShowASingleChat(Long senderId, Long receiverId) {
        return messageRepository.findBySenderIdAndReceiverId(senderId, receiverId);
    }

    public List<UserDTO> ShowAllSendersByReceiverId(Long receiverId) {
        return messageUserRepository.ShowAllSendersByReceiverId(receiverId);
    }

    public void deleteAllMessagesByConversationId(Long conversationId) {
        List<MessageModel> messages = messageRepository.findByConversationId(conversationId);
        for (MessageModel message : messages) {
            messageRepository.delete(message);
        }
    }

    public void SendMessage(MessageModel message) {
        messageRepository.save(message);
    }

    public void MarkAsRead(Long sender_id, Long receiver_id) {
        messageRepository.MarkAsRead(sender_id, receiver_id);
    }

    public Long GetUnreadMessageCount(Long id) throws ResourceNotFoundException
    {
        Long number = messageRepository.GetUnreadMessageCount(id);
        if (number < 0) {
            throw new ResourceNotFoundException("No unread messages found for this id :: " + id);
        }
        return number;
    }
    
}
