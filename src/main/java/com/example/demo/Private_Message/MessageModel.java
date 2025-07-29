package com.example.demo.Private_Message;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "receiver_id")
    private Long receiverId;

    @Column(name = "content")
    private String content;

    @Column(name = "timestamp")
    private LocalDate timestamp;

    @Column(name = "status")
    private String isRead;

    @Column(name = "conversation_id")
    private Long conversationId;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Column(name = "message_type")
    private String messageType;

    public Long getReceiverId() {
        return receiverId;
    }

    public void setTimestamp(LocalDate now) {
        this.timestamp = now;
    }
}
