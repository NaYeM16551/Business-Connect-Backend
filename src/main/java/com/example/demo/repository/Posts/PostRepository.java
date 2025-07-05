package com.example.demo.repository.Posts;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Posts.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // Add custom query methods if needed
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.media WHERE p.id = :postId")
    Optional<Post> findByIdWithMedia(@Param("postId") Long postId);

    @Query("SELECT DISTINCT p FROM Post p LEFT JOIN FETCH p.media WHERE p.user.id = :userId")
    Optional<List<Post>> getPostsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Post p " +
           "SET p.parentPostId = -1 " +
           "WHERE p.parentPostId = :deletedId")
    int unsetParentForChildren(@Param("deletedId") Long deletedId);

    @Modifying
    @Query("DELETE FROM Post p WHERE p.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);
}
