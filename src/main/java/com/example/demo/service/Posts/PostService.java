package com.example.demo.service.Posts;

import java.io.IOException;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.User;
import com.example.demo.model.Posts.Post;
import com.example.demo.model.Posts.PostMedia;
import com.example.demo.repository.Posts.PostMediaRepository;
import com.example.demo.repository.Posts.PostRepository;
import com.example.demo.service.CloudinaryService;

@Service
public class PostService {

    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private PostRepository postRepo;
    @Autowired
    private PostMediaRepository mediaRepo;

    public Long createPost(String content, MultipartFile[] files, Principal principal) throws IOException {
        Long userId = Long.valueOf(principal.getName());

        Post post = new Post();
        post.setContent(content);
        post.setCreatedAt(java.time.LocalDateTime.now());

        // Set User (only set ID for reference)
        User user = new User();
        user.setId(userId);
        post.setUser(user);

        post = postRepo.save(post);

        for (MultipartFile file : files) {
            String url = cloudinaryService.uploadFile(file);

            PostMedia media = new PostMedia();
            media.setMediaUrl(url);
            media.setMediaType(file.getContentType()); // <-- Set mediaType!
            media.setPost(post);

            mediaRepo.save(media);
        }

        return post.getId();
    }

}
