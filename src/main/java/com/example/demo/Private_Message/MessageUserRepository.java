package com.example.demo.Private_Message;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.model.User;

@Repository
public interface MessageUserRepository extends JpaRepository<User, Long> {
   
    @Query(value = "SELECT DISTINCT ON (s.id) s.id, s.email, s.profile_picture_url, s.username\n" + //
                "    FROM chat_messages c\n" + //
                "    JOIN users s ON c.sender_id = s.id\n" + //
                "    WHERE c.receiver_id = ?1\n" + //
                "    ORDER BY s.id, c.timestamp DESC", nativeQuery = true)
    List<UserDTO> ShowAllSendersByReceiverId(Long receiverId);

}
