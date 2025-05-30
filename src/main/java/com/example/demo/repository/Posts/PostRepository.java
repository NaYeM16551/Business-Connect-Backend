package com.example.demo.repository.Posts;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.model.Posts.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    // Add custom query methods if needed
}
