package com.example.demo.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.Groups.CreateGroupRequest;
import com.example.demo.dto.Groups.GroupMemberResponse;
import com.example.demo.dto.Groups.GroupResponse;
import com.example.demo.model.Groups.Group;
import com.example.demo.model.Groups.GroupMembership;
import com.example.demo.service.Groups.GroupService;
import com.example.demo.service.Posts.PostService;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private PostService postService;

    // Create a new group
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@RequestBody CreateGroupRequest request,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        System.out.println("Group type from request: " + request.getType());
        Group group = groupService.createGroup(request.getName(),request.getType(), request.getDescription(), request.getPrivacy(),
                userId);
        GroupResponse response = groupService.getGroupById(group.getId(), userId);
        return ResponseEntity.ok(response);
    }

    // Get group by ID
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable Long groupId, Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        GroupResponse response = groupService.getGroupById(groupId, userId);
        return ResponseEntity.ok(response);
    }

    // Get all groups for a user
    @GetMapping("/my-groups")
    public ResponseEntity<List<GroupResponse>> getMyGroups(Authentication authentication) {
        System.out.println("Authentication: " + authentication);
        try {
            // Get user ID from authentication
            Long userId = Long.valueOf(authentication.getName());
            System.out.println("userId: " + userId);
            List<GroupResponse> groups = groupService.getGroupsByUserId(userId);
            System.out.println("Groups fetched: " + groups);
            return ResponseEntity.ok(groups);
        } catch (NumberFormatException e) {
            // Handle case where authentication name is not a valid Long
            return ResponseEntity.badRequest().body(null);
        }

    }

     @GetMapping("/recommended-groups")
    public ResponseEntity<List<GroupResponse>> getRecommendedGroups(Authentication authentication) {
        System.out.println("Authentication: " + authentication);
        try {
            // Get user ID from authentication
            Long userId = Long.valueOf(authentication.getName());
            System.out.println("userId: " + userId);
            List<GroupResponse> groups = groupService.getGroupsByUserRole(userId);
            System.out.println("Groups fetched: " + groups);
            return ResponseEntity.ok(groups);
        } catch (NumberFormatException e) {
            // Handle case where authentication name is not a valid Long
            return ResponseEntity.badRequest().body(null);
        }

    }

    // Search groups
   @GetMapping("/search")
public ResponseEntity<?> searchGroups(
        @RequestParam(value = "query", required = false, defaultValue = "") String query,
        Authentication authentication) {
    try {
        Long userId = Long.valueOf(authentication.getName());
        System.out.println("Searching groups with query: '" + query + "' for user: " + userId);

        List<GroupResponse> groups = groupService.searchGroups(query, userId);
        System.out.println("Found " + groups.size() + " groups");

        return ResponseEntity.ok(groups);

    } catch (NumberFormatException e) {
        System.err.println("Invalid user ID format: " + e.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid user authentication", "message", e.getMessage()));

    } catch (RuntimeException e) {
        System.err.println("Runtime error searching groups: " + e.getMessage());
        return ResponseEntity.status(500)
                .body(Map.of("error", "Internal server error", "message", e.getMessage()));

    } catch (Exception e) {
        System.err.println("Unexpected error searching groups: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(500)
                .body(Map.of("error", "Unexpected error", "message", "An unexpected error occurred"));
    }
}


    // Join a group
    @PostMapping("/{groupId}/join")
    public ResponseEntity<String> joinGroup(@PathVariable Long groupId, Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        groupService.joinGroup(groupId, userId);
        return ResponseEntity.ok("Successfully joined the group");
    }

    // Leave a group
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<String> leaveGroup(@PathVariable Long groupId, Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        groupService.leaveGroup(groupId, userId);
        return ResponseEntity.ok("Successfully left the group");
    }

    // Get group members
    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponse>> getGroupMembers(@PathVariable Long groupId,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        List<GroupMemberResponse> members = groupService.getGroupMembers(groupId, userId);
        return ResponseEntity.ok(members);
    }

    // Update group role
    @PutMapping("/{groupId}/members/{memberId}/role")
    public ResponseEntity<String> updateMemberRole(
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            @RequestParam String role,
            Authentication authentication) {
        Long adminUserId = Long.valueOf(authentication.getName());
        GroupMembership.Role newRole = GroupMembership.Role.valueOf(role.toUpperCase());
        groupService.updateGroupRole(groupId, memberId, adminUserId, newRole);
        return ResponseEntity.ok("Member role updated successfully");
    }

    // Remove member from group
    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<String> removeMember(
            @PathVariable Long groupId,
            @PathVariable Long memberId,
            Authentication authentication) {
        Long adminUserId = Long.valueOf(authentication.getName());
        groupService.removeMember(groupId, memberId, adminUserId);
        return ResponseEntity.ok("Member removed successfully");
    }

    // Update group settings
    @PutMapping("/{groupId}")
    public ResponseEntity<String> updateGroup(
            @PathVariable Long groupId,
            @RequestBody CreateGroupRequest request,
            Authentication authentication) {
        Long adminUserId = Long.valueOf(authentication.getName());
        groupService.updateGroup(groupId, adminUserId, request.getName(), request.getDescription(),
                request.getPrivacy(), request.getCoverImage());
        return ResponseEntity.ok("Group updated successfully");
    }

    // Delete group
    @DeleteMapping("/{groupId}")
    public ResponseEntity<String> deleteGroup(@PathVariable Long groupId, Authentication authentication) {
        Long ownerId = Long.valueOf(authentication.getName());
        groupService.deleteGroup(groupId, ownerId);
        return ResponseEntity.ok("Group deleted successfully");
    }

    // Create a post in a group
    @PostMapping("/{groupId}/posts")
    public ResponseEntity<Long> createGroupPost(
            @PathVariable Long groupId,
            @RequestParam String content,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            Authentication authentication) throws IOException {
        Long userId = Long.valueOf(authentication.getName());

        // Check if user can post in group
        if (!groupService.canPostInGroup(groupId, userId)) {
            return ResponseEntity.badRequest().body(null);
        }

        // Create post with group association
        Long postId = postService.createGroupPost(content, files, userId, groupId);
        return ResponseEntity.ok(postId);
    }

    // Get posts from a group
    @GetMapping("/{groupId}/posts")
    public ResponseEntity<List<com.example.demo.dto.Posts.PostDto>> getGroupPosts(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());

        // Check if user is member of group
        if (!groupService.isMember(groupId, userId)) {
            System.out.println("User " + userId + " is not a member of group " + groupId);
            return ResponseEntity.badRequest().body(null);
        }
        System.out.println("Fetching posts for group " + groupId + " with page " + page + " and size " + size);

        List<com.example.demo.dto.Posts.PostDto> posts = postService.getPostsByGroupId(groupId, page, size);
        return ResponseEntity.ok(posts);
    }

    // Test endpoint for debugging
    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        try {
            System.out.println("Groups test endpoint called");
            List<GroupResponse> allGroups = groupService.searchGroups("", 1L); // Test with empty search
            return ResponseEntity.ok("Groups service is working. Found groups: " + allGroups.size());
        } catch (Exception e) {
            System.err.println("Error in test endpoint: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}