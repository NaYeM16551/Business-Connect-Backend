package com.example.demo.AI_Chatbot;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AIRepository extends JpaRepository<AIModel, Long> {
    
    /**
     * Find the latest chat history for a user, limited by the specified count
     * @param userId The user ID
     * @param pageable Pagination parameters (to limit results)
     * @return List of recent chat history entries
     */
    @Query("SELECT a FROM AIModel a WHERE a.userId = :userId ORDER BY a.createdAt DESC")
    List<AIModel> findLatestChatHistoryByUserId(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Find all chat history for a user ordered by creation date descending
     * @param userId The user ID
     * @return List of all chat history entries for the user
     */
    List<AIModel> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * Count total chat entries for a user
     * @param userId The user ID
     * @return Total count of chat entries
     */
    long countByUserId(Long userId);
    
    /**
     * Find chat history by user ID with pagination
     * @param userId The user ID
     * @param pageable Pagination parameters
     * @return List of chat history entries
     */
    List<AIModel> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}