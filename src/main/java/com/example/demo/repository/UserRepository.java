package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

import com.example.demo.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByResetPasswordToken(String username);

    @EntityGraph(attributePaths = {
            "industry",
            "interests",
            "achievements"
    })
    @NonNull
    Optional<User> findById(@NonNull Long id);

    // Find users by role (excluding a specific user ID)
    @Query("SELECT u FROM User u WHERE u.role IN :roles AND u.id != :excludeUserId")
    List<User> findByRoleInAndIdNot(@Param("roles") List<String> roles, @Param("excludeUserId") Long excludeUserId);

    // Find users by role
    List<User> findByRoleIn(List<String> roles);

}
