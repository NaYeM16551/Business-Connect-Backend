package com.example.demo.dto.User;

import java.util.List;

public class UserMeResponse {
    private Long id;
    private String username;
    private String email;
    private String role;
    private List<String> industry;
    private List<String> interests;
    private List<String> achievements;
    private String profilePictureUrl;

    // Constructors
    public UserMeResponse() {
    }

    public UserMeResponse(Long id, String username, String email, String role,
            List<String> industry, List<String> interests,
            List<String> achievements, String profilePictureUrl) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.industry = industry;
        this.interests = interests;
        this.achievements = achievements;
        this.profilePictureUrl = profilePictureUrl;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<String> getIndustry() {
        return industry;
    }

    public void setIndustry(List<String> industry) {
        this.industry = industry;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public List<String> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<String> achievements) {
        this.achievements = achievements;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
}
