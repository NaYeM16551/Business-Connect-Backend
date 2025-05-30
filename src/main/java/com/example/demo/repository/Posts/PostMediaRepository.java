package com.example.demo.repository.Posts;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.Posts.PostMedia;

public interface PostMediaRepository extends JpaRepository<PostMedia, Long> {
    // Add custom query methods if needed
}


