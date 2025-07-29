package com.example.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users_verify")
public class RegisterVerifyUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;
    private String verificationToken;
    private LocalDateTime verificationTokenExpiry;
    private boolean isVerified = false;

    public RegisterVerifyUser() {
    };

    public RegisterVerifyUser(String email, String verificationToken, LocalDateTime verificationTokenExpiry) {
        this.email = email;
        this.verificationToken = verificationToken;
        this.verificationTokenExpiry = verificationTokenExpiry;
    }

    // generate getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    public void setVerificationTokenExpiry(LocalDateTime localDateTime) {
        this.verificationTokenExpiry = localDateTime;
    }

    public LocalDateTime getVerificationTokenExpiry() {
        return verificationTokenExpiry;
    }

    public void setIsVerified(boolean value) {
        this.isVerified = value;
    }

    public boolean getIsVerified() {
        return isVerified;
    }

}
