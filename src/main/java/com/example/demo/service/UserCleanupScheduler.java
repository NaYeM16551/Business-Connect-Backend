// src/main/java/com/example/demo/scheduler/UserCleanupScheduler.java

package com.example.demo.service;

import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserCleanupScheduler {

    private final UserRepository userRepository;

    public UserCleanupScheduler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 0 * * * *") // every hour
    @Transactional
    public void deleteStaleUnverifiedUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusSeconds(50);
        userRepository.deleteAllByIsVerifiedFalseAndVerificationTokenExpiryBefore(cutoff);
        System.out.println("🧹 Old unverified users cleaned up");
    }
}
