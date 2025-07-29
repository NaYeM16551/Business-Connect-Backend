package com.example.demo.Contest;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "questions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionModel{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "contest_id", nullable = false)
    private Long contestId;

    @Column(name = "title" ,columnDefinition = "TEXT")
    private String title;
    
    @Column(name = "marks")
    private Long marks;
    
    @Column(name = "attachment_url", columnDefinition = "TEXT")
    private String attachmentUrl;

    // Getters and Setters
}

