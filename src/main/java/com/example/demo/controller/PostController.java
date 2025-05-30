package com.example.demo.controller;

import com.example.demo.service.Posts.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/post")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping(
        value = "/create-post",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> createPost(
            @RequestParam("content") String content,
            @RequestParam("files") MultipartFile[] files,
            Principal principal
    ) {
        try {
            

            // 3) Delegate to service (which may throw business exceptions or IO errors)
            Long postId = postService.createPost(content, files, principal);

            // 4) Successful creation
            return ResponseEntity.ok(Map.of(
                    "message", "Post created",
                    "postId",   postId
            ));
        } catch (Exception e) {
          
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
