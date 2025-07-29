package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.demo.model.User;
import com.example.demo.model.Groups.Group;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.Groups.GroupRepository;
import com.example.demo.service.Groups.GroupService;

/**
 * Service to initialize sample data for testing groups functionality
 */
@Component
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupService groupService;

    @Override
    public void run(String... args) throws Exception {
        // Only create sample data if groups table is empty
        if (groupRepository.count() == 0) {
            System.out.println("Creating sample groups for testing...");
            createSampleGroups();
        }
    }

    private void createSampleGroups() {
        try {
            // Find an existing user or create one
            User user = userRepository.findAll().stream()
                    .findFirst()
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setUsername("testuser");
                        newUser.setEmail("test@example.com");
                        newUser.setPassword("password");
                        return userRepository.save(newUser);
                    });

            // Create sample groups
            groupService.createGroup(
                    "Tech Enthusiasts",
                    "research",
                    "A group for technology lovers and innovators",
                    Group.Privacy.PUBLIC,
                    user.getId());

            groupService.createGroup(
                    "Business Network",
                    "invest",
                    "Connect with business professionals and entrepreneurs",
                    Group.Privacy.PUBLIC,
                    user.getId());

            groupService.createGroup(
                    "Developers Hub",
                    "Business",
                    "Share coding tips, projects, and collaborate on development",
                    Group.Privacy.PUBLIC,
                    user.getId());

            System.out.println("Sample groups created successfully!");
        } catch (Exception e) {
            System.err.println("Error creating sample groups: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
