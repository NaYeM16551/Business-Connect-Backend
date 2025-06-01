package com.example.demo.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import com.example.demo.model.User;

@DataJpaTest // spins up only the JPA layer (in‐memory H2 by default)
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    void findByEmail_returnsUser_whenExists() {
        // ─── ARRANGE ────────────────────────────────────────────────────────
        User user = new User();
        user.setUsername("M.M.Nabil");
        user.setEmail("nabil2005@gmail.com");
        user.setPassword("nayem123");
        user.setIndustry(List.of("Technology", "Finance"));
        user.setInterests(List.of("Machine Learning", "Blockchain"));
        user.setAchievements(List.of("Published a research paper", "Speaker at TechConf 2024"));

        userRepository.save(user);

        // ─── ACT ───────────────────────────────────────────────────────────
        Optional<User> found = userRepository.findByEmail("nabil2005@gmail.com");

        // ─── ASSERT ───────────────────────────────────────────────────────
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("M.M.Nabil");
        assertThat(found.get().getEmail()).isEqualTo("nabil2005@gmail.com");
    }

    @Test
    void findByEmail_returnsEmpty_whenNotExists() {
        // ACT
        Optional<User> found = userRepository.findByEmail("unknown@example.com");

        // ASSERT
        assertThat(found).isNotPresent();
    }

    @Test
    void findByResetPasswordToken_returnsUser_whenExists() {
        // ─── ARRANGE ────────────────────────────────────────────────────────
        User user = new User();
        user.setUsername("M.M.Nabil");
        user.setEmail("nabil2005@gmail.com"); // Keep this unique among tests
        user.setPassword("nayem123");
        user.setIndustry(List.of("Technology", "Finance"));
        user.setInterests(List.of("Machine Learning", "Blockchain"));
        user.setAchievements(List.of("Published a research paper", "Speaker at TechConf 2024"));
        user.setResetPasswordToken("reset-token-123");

        userRepository.save(user);

        // ─── ACT ───────────────────────────────────────────────────────────
        Optional<User> found = userRepository.findByResetPasswordToken("reset-token-123");

        // ─── ASSERT ───────────────────────────────────────────────────────
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("M.M.Nabil");
        assertThat(found.get().getEmail()).isEqualTo("nabil2005@gmail.com");
        assertThat(found.get().getResetPasswordToken()).isEqualTo("reset-token-123");
    }

    @Test
    void findByResetPasswordToken_returnsEmpty_whenNotExists() {
        // ACT
        Optional<User> found = userRepository.findByResetPasswordToken("non-existent-token");

        // ASSERT
        assertThat(found).isNotPresent();
    }

    @Test
    void checkForDuplication_throwsWhenEmailIsNotUnique() {
        // ─── ARRANGE ────────────────────────────────────────────────────────
        User user1 = new User();
        user1.setUsername("M.M.Nabil");
        user1.setEmail("nabil2005@gmail.com");
        user1.setPassword("nayem123");
        user1.setIndustry(List.of("Technology", "Finance"));
        user1.setInterests(List.of("Machine Learning", "Blockchain"));
        user1.setAchievements(List.of("Published a research paper", "Speaker at TechConf 2024"));
        userRepository.saveAndFlush(user1); // first user is saved/flushed

        // Now create a second user with the *same* email
        User user2 = new User();
        user2.setUsername("Duplicate");
        user2.setEmail("nabil2005@gmail.com");
        user2.setPassword("otherpass");
        user2.setIndustry(List.of("Tech"));
        user2.setInterests(List.of("AI"));
        user2.setAchievements(List.of("Another achievement"));

        // ─── ACT & ASSERT ────────────────────────────────────────────────────
        // We must call saveAndFlush (or save + flush) so that Hibernate issues the INSERT immediately
        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
            .isInstanceOf(DataIntegrityViolationException.class);
    }
}
