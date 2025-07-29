package com.example.demo.Private_Message;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.User;
import jakarta.transaction.Transactional;

@Repository
public interface MessageRepository extends JpaRepository<MessageModel, Long> {
    @Query(value = "SELECT * FROM chat_messages WHERE (sender_id = ?1 AND receiver_id = ?2) OR (sender_id = ?2 AND receiver_id = ?1)", nativeQuery = true)
    List<MessageModel> findBySenderIdAndReceiverId(Long senderId, Long receiverId);

    // @Query("""
    // SELECT DISTINCT s
    // FROM ChatMessage c
    // JOIN StudentModel s ON c.senderId = s.id
    // WHERE c.receiverId = :receiverId
    // ORDER BY c.timestamp DESC
    // """)
    // List<StudentModel> ShowAllSendersByReceiverId(@Param("receiverId") Long receiverId);


    List<MessageModel> findByConversationId(Long conversationId);
    
    @Transactional
    @Modifying
    @Query(value = "UPDATE chat_messages SET status = 'Seen' WHERE sender_id  = ?1 AND receiver_id = ?2 AND status = 'Sent'", nativeQuery = true)
    void MarkAsRead(Long sender_id, Long receiver_id);
    
    @Query(value = "SELECT COUNT(DISTINCT sender_id) FROM chat_messages WHERE receiver_id = ?1 AND status = 'Sent'", nativeQuery = true)
    Long GetUnreadMessageCount(Long id);

}
