package com.example.demo.Private_Message;
import com.example.demo.model.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;


public class UserDTO {
    public Long id;
    public String username;
    public  String email;
    public String profilePictureUrl;

    public UserDTO(Long id, String email, String profilePictureUrl, String username) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.profilePictureUrl = profilePictureUrl;
    }
    // Getters and setters
}
