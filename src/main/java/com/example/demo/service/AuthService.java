package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.RegisterResponse;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Value("${frontend.url}")
    private String frontendUrl;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder,
            MailService mailService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    public RegisterResponse register(RegisterRequest request) {

        Optional<User> optional = userRepository.findByEmail(request.email);

        User user;
        if (optional.isPresent()) {
            user = optional.get();
            System.out.println("User already exists");
            if (user.getIsVerified()) {
                throw new IllegalArgumentException("Email already registered.");
            }
        } else {
            System.out.println("Creating new user");
            user = new User();
        }
        user.setUsername(request.username);
        user.setEmail(request.email);
        user.setPassword(passwordEncoder.encode(request.password));
        user.setIndustry(request.industry);
        user.setInterests(request.interests);
        user.setAchievements(request.achievements);
        user.setIsVerified(false);

        String token = UUID.randomUUID().toString();

        boolean shouldGenerateToken = !optional.isPresent()
                || (optional.isPresent() && user.getVerificationTokenExpiry().isBefore(LocalDateTime.now()));

        if (shouldGenerateToken) {
            System.out.println("Generating new token");
            token = UUID.randomUUID().toString();
            user.setVerificationToken(token);
        }

        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));

        User savedUser = userRepository.save(user);

        String url = "http://localhost:8080/api/v1/auth/verify-email?token=";

        mailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken(), url);

        return new RegisterResponse(
                "Registration successful",
                Map.of("id", "u_" + savedUser.getId(), "email", savedUser.getEmail()));
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        if (!user.getIsVerified()) {
            throw new IllegalArgumentException("Please verify your email before logging in.");
        }

        if (!passwordEncoder.matches(request.password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return new LoginResponse(
                "Login successful",
                Map.of("id", "u_" + user.getId(), "email", user.getEmail()),
                token);
    }

    public void sendPasswordResetEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email not registered."));

        String token = jwtUtil.generateToken(user.getEmail());
        user.setVerificationToken(token);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        String url = frontendUrl + "/reset-password?token=";
        mailService.sendVerificationEmail(user.getEmail(), token, url);
    }

    public void verifyEmailToken(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token."));

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification token has expired.");
        }

        user.setIsVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
    }

    public void updateProfile(String token, RegisterRequest request) {
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setUsername(request.username);
        // user.setEmail(request.email);
        user.setPassword(passwordEncoder.encode(request.password));
        user.setIndustry(request.industry);
        user.setInterests(request.interests);
        user.setAchievements(request.achievements);
        userRepository.save(user);

    }

    public void updatePassword(String token, String newPassword) {
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // user.setEmail(request.email);
        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

    }

}
