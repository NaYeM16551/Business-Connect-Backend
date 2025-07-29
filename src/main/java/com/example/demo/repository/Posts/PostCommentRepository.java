package com.example.demo.repository.Posts;


import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Posts.PostComment;  
import java.util.List;  

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {

    List<PostComment> findByPostId(Long postId);
}