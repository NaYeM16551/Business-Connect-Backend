package com.example.demo.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import com.example.demo.model.RegisterVerifyUser;

public interface RegisterVerifyUserRepository  extends JpaRepository<RegisterVerifyUser, Long> {
    Optional<RegisterVerifyUser> findByEmail(String email);
    Optional<RegisterVerifyUser> findByVerificationToken(String token);
    void deleteAllByIsVerifiedFalseAndVerificationTokenExpiryBefore(LocalDateTime cutoff);
    long deleteByEmail(String email);

    
} 