package com.example.demo.repository.Posts;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.Posts.PostMedia;

import java.util.List;

public interface PostMediaRepository extends JpaRepository<PostMedia, Long> {
    // Add custom query methods if needed
    List<PostMedia> findAllByPostId(Long postId); // Example method to find all media by post ID
}


