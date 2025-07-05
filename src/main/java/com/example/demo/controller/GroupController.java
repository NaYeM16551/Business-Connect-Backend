package com.example.demo.controller;

import com.example.demo.dto.Groups.*;
import com.example.demo.model.Groups.Group;
import com.example.demo.model.Groups.GroupMembership;
import com.example.demo.service.Groups.GroupService;
import com.example.demo.service.Posts.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
@CrossOrigin(origins = "*")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private PostService postService;

    // Create a new group
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@RequestBody CreateGroupRequest request, Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        Group group = groupService.createGroup(request.getName(), request.getDescription(), request.getPrivacy(), userId);
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
        Long userId = Long.valueOf(authentication.getName());
        List<GroupResponse> groups = groupService.getGroupsByUserId(userId);
        return ResponseEntity.ok(groups);
    }

    // Search groups
    @GetMapping("/search")
    public ResponseEntity<List<GroupResponse>> searchGroups(@RequestParam String query, Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        List<GroupResponse> groups = groupService.searchGroups(query, userId);
        return ResponseEntity.ok(groups);
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
    public ResponseEntity<List<GroupMemberResponse>> getGroupMembers(@PathVariable Long groupId, Authentication authentication) {
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
        groupService.updateGroup(groupId, adminUserId, request.getName(), request.getDescription(), request.getPrivacy(), request.getCoverImage());
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
            return ResponseEntity.badRequest().body(null);
        }

        List<com.example.demo.dto.Posts.PostDto> posts = postService.getPostsByGroupId(groupId, page, size);
        return ResponseEntity.ok(posts);
    }
} 