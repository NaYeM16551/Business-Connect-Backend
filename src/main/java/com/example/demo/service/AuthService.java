package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.RegisterResponse;
import com.example.demo.dto.RegisterVerifyResponse;
import com.example.demo.model.RegisterVerifyUser;
import com.example.demo.model.User;
import com.example.demo.repository.RegisterVerifyUserRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final RegisterVerifyUserRepository registerVerifyUserRepository;

    @Value("${frontend.url}")
    private String frontendUrl;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder,
            MailService mailService, RegisterVerifyUserRepository registerVerifyUserRepository) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.registerVerifyUserRepository = registerVerifyUserRepository;
    }

    public RegisterVerifyResponse registerVerify(String email) {
        Optional<RegisterVerifyUser> optional = registerVerifyUserRepository.findByEmail(email);

        RegisterVerifyUser user;
        if (optional.isPresent()) {
            user = optional.get();
            System.out.println("User already exists");
            if (user.getIsVerified()) {
                throw new IllegalArgumentException("Email already registered.");
            }
        } else {
            System.out.println("Creating new user");
            user = new RegisterVerifyUser();
        }

        user.setEmail(email);
        user.setIsVerified(false);

        boolean shouldGenerateToken = !optional.isPresent()
                || (optional.isPresent() && user.getVerificationTokenExpiry().isBefore(LocalDateTime.now()));

        if (shouldGenerateToken) {
            System.out.println("Generating new token");
            String token = UUID.randomUUID().toString();
            user.setVerificationToken(token);
        }

        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));

        registerVerifyUserRepository.save(user);

        String url = "http://localhost:8080/api/v1/auth/verify-email?token=";

        mailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken(), url);

        String token = jwtUtil.generateToken(user.getEmail());
        return new RegisterVerifyResponse(
                "Verification email sent", token);
    }

    public RegisterResponse register(RegisterRequest request, String token) {

        String email = jwtUtil.extractEmail(token);

        if (!email.equals(request.email)) {
            throw new IllegalArgumentException("Email mismatch");
        }

        Optional<RegisterVerifyUser> optionalVerifyUser = registerVerifyUserRepository.findByEmail(email);

        if ((optionalVerifyUser.isPresent() && !optionalVerifyUser.get().getIsVerified())
                || !optionalVerifyUser.isPresent()) {
            throw new IllegalArgumentException("Email not verified");

        }

        Optional<User> optional = userRepository.findByEmail(request.email);

        User user;
        if (optional.isPresent()) {
            throw new IllegalArgumentException("Email already registered.");

        } else {
            System.out.println("Creating new user");
            user = new User();
        }

        // registerVerifyUserRepository.deleteByEmail(email);

        user.setUsername(request.username);
        user.setEmail(request.email);
        user.setPassword(passwordEncoder.encode(request.password));
        user.setIndustry(request.industry);
        user.setInterests(request.interests);
        user.setAchievements(request.achievements);

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                "Registration successful",
                Map.of("id", "u_" + savedUser.getId(), "email", savedUser.getEmail()));
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email  or not verified."));

        if (!passwordEncoder.matches(request.password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        return new LoginResponse(
                "Login successful",
                Map.of("id", "u_" + user.getId(), "email", user.getEmail()),
                token);
    }

    public void sendPasswordResetEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email not registered or not verified."));

        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        String url = frontendUrl + "/reset-password?token=";
        mailService.sendVerificationEmail(user.getEmail(), token, url);
    }

    public void verifyEmailToken(String token) {
        RegisterVerifyUser user = registerVerifyUserRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid verification token."));

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification token has expired.");
        }

        user.setIsVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        registerVerifyUserRepository.save(user);
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

    @Transactional
    public void deleteAccount(String token) {
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete the user
        userRepository.delete(user);

        // Delete the verification token
        Optional<RegisterVerifyUser> optionalVerifyUser = registerVerifyUserRepository.findByEmail(email);
        if (optionalVerifyUser.isPresent()) {
            RegisterVerifyUser verifyUser = optionalVerifyUser.get();
            registerVerifyUserRepository.delete(verifyUser);
        }
    }

    public void updatePassword(String token, String newPassword) {

        Optional<User> optional = userRepository.findByResetPasswordToken(token);
        if (!optional.isPresent() || token == null) {
            throw new IllegalArgumentException("Invalid token");
        }
        User user = optional.get();
        if (user.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token has expired.");
        }

        // user.setEmail(request.email);
        user.setPassword(passwordEncoder.encode(newPassword));

        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);

    }

    public void changePassword(String token, String newPassword,String oldPassword) {
        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if(!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

}
