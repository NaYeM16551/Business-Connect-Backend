package com.example.demo.service.Groups;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.Groups.GroupMemberResponse;
import com.example.demo.dto.Groups.GroupResponse;
import com.example.demo.model.User;
import com.example.demo.model.Groups.Group;
import com.example.demo.model.Groups.GroupMembership;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.Groups.GroupMembershipRepository;
import com.example.demo.repository.Groups.GroupRepository;
import com.example.demo.repository.Posts.PostRepository;

@Service
@Transactional
public class GroupService {

    private static final Logger logger = LoggerFactory.getLogger(GroupService.class);

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupMembershipRepository membershipRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    /**
     * Create a new group
     */
    public Group createGroup(String name, String description, Group.Privacy privacy, Long ownerId) {
        logger.info("Creating group: {} for owner: {}", name, ownerId);

        try {
            // Input validation
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Group name cannot be empty");
            }
            if (ownerId == null) {
                throw new IllegalArgumentException("Owner ID cannot be null");
            }

            // Find and validate owner
            User owner = userRepository.findById(ownerId)
                    .orElseThrow(() -> new IllegalArgumentException("Owner user not found: " + ownerId));

            // Create and save group
            Group group = new Group();
            group.setName(name.trim());
            group.setDescription(description != null ? description.trim() : "");
            group.setPrivacy(privacy != null ? privacy : Group.Privacy.PUBLIC);
            group.setOwner(owner);
            group.setMemberCount(0);
            group.setPostCount(0);

            group = groupRepository.save(group);
            logger.info("Group created with ID: {}", group.getId());

            // Create owner membership
            GroupMembership ownerMembership = new GroupMembership();
            ownerMembership.setGroup(group);
            ownerMembership.setUser(owner);
            ownerMembership.setRole(GroupMembership.Role.OWNER);
            membershipRepository.save(ownerMembership);

            // Update member count
            group.setMemberCount(1);
            groupRepository.save(group);

            logger.info("Group created successfully: {}", group.getId());
            return group;

        } catch (IllegalArgumentException e) {
            logger.error("Invalid input for group creation: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Error creating group: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create group", e);
        }
    }

    /**
     * Join a group
     */
    public void joinGroup(Long groupId, Long userId) {
        logger.info("User {} joining group {}", userId, groupId);

        try {
            // Input validation
            validateIds(groupId, userId);

            // Find group
            Group group = findGroupById(groupId);

            // Check if user exists
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

            // Check if already a member
            if (membershipRepository.existsByGroupIdAndUserId(groupId, userId)) {
                throw new IllegalStateException("User is already a member of this group");
            }

            // Check privacy settings
            if (group.getPrivacy() != Group.Privacy.PUBLIC) {
                throw new IllegalStateException("Can only join public groups directly");
            }

            // Create membership
            GroupMembership membership = new GroupMembership();
            membership.setGroup(group);
            membership.setUser(user);
            membership.setRole(GroupMembership.Role.MEMBER);
            membershipRepository.save(membership);

            // Update member count
            group.setMemberCount(group.getMemberCount() + 1);
            groupRepository.save(group);

            logger.info("User {} successfully joined group {}", userId, groupId);

        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Error joining group: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error joining group: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to join group", e);
        }
    }

    /**
     * Leave a group
     */
    public void leaveGroup(Long groupId, Long userId) {
        logger.info("User {} leaving group {}", userId, groupId);

        try {
            // Input validation
            validateIds(groupId, userId);

            // Find group
            Group group = findGroupById(groupId);

            // Find membership
            GroupMembership membership = membershipRepository.findByGroupIdAndUserId(groupId, userId)
                    .orElseThrow(() -> new IllegalArgumentException("User is not a member of this group"));

            // Owner cannot leave
            if (membership.getRole() == GroupMembership.Role.OWNER) {
                throw new IllegalStateException("Group owner cannot leave the group");
            }

            // Remove membership
            membershipRepository.delete(membership);

            // Update member count
            if (group.getMemberCount() > 0) {
                group.setMemberCount(group.getMemberCount() - 1);
                groupRepository.save(group);
            }

            logger.info("User {} successfully left group {}", userId, groupId);

        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Error leaving group: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error leaving group: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to leave group", e);
        }
    }

    /**
     * Update group role (for admin/owner actions)
     */
    public void updateGroupRole(Long groupId, Long userId, Long adminUserId, GroupMembership.Role newRole) {
        logger.info("Admin {} updating role for user {} in group {} to {}", adminUserId, userId, groupId, newRole);

        try {
            // Input validation
            validateIds(groupId, userId);
            validateIds(groupId, adminUserId);
            if (newRole == null) {
                throw new IllegalArgumentException("New role cannot be null");
            }

            // Check admin permissions
            validateAdminPermissions(groupId, adminUserId);

            // Find target membership
            GroupMembership membership = membershipRepository.findByGroupIdAndUserId(groupId, userId)
                    .orElseThrow(() -> new IllegalArgumentException("Target user is not a member of this group"));

            // Cannot change owner's role
            if (membership.getRole() == GroupMembership.Role.OWNER) {
                throw new IllegalStateException("Cannot change owner's role");
            }

            // Update role
            membership.setRole(newRole);
            membershipRepository.save(membership);

            logger.info("Role updated successfully for user {} in group {}", userId, groupId);

        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Error updating group role: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error updating group role: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update group role", e);
        }
    }

    /**
     * Remove a member from the group
     */
    public void removeMember(Long groupId, Long userId, Long adminUserId) {
        logger.info("Admin {} removing user {} from group {}", adminUserId, userId, groupId);

        try {
            // Input validation
            validateIds(groupId, userId);
            validateIds(groupId, adminUserId);

            // Check admin permissions
            validateAdminPermissions(groupId, adminUserId);

            // Find target membership
            GroupMembership membership = membershipRepository.findByGroupIdAndUserId(groupId, userId)
                    .orElseThrow(() -> new IllegalArgumentException("Target user is not a member of this group"));

            // Cannot remove owner
            if (membership.getRole() == GroupMembership.Role.OWNER) {
                throw new IllegalStateException("Cannot remove group owner");
            }

            // Remove membership
            membershipRepository.delete(membership);

            // Update member count
            Group group = findGroupById(groupId);
            if (group.getMemberCount() > 0) {
                group.setMemberCount(group.getMemberCount() - 1);
                groupRepository.save(group);
            }

            logger.info("User {} successfully removed from group {}", userId, groupId);

        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Error removing member: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error removing member: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to remove member", e);
        }
    }

    /**
     * Update group settings
     */
    public void updateGroup(Long groupId, Long adminUserId, String name, String description,
            Group.Privacy privacy, String coverImage) {
        logger.info("Updating group {} by user {}", groupId, adminUserId);

        try {
            // Input validation
            validateIds(groupId, adminUserId);

            // Find group
            Group group = findGroupById(groupId);

            // Check permissions
            validateAdminPermissions(groupId, adminUserId);

            // Update fields
            if (name != null && !name.trim().isEmpty()) {
                group.setName(name.trim());
            }
            if (description != null) {
                group.setDescription(description.trim());
            }
            if (privacy != null) {
                group.setPrivacy(privacy);
            }
            if (coverImage != null) {
                group.setCoverImage(coverImage.trim());
            }

            groupRepository.save(group);
            logger.info("Group {} updated successfully", groupId);

        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Error updating group: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error updating group: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to update group", e);
        }
    }

    /**
     * Delete a group
     */
    public void deleteGroup(Long groupId, Long ownerId) {
        logger.info("Deleting group {} by owner {}", groupId, ownerId);

        try {
            // Input validation
            validateIds(groupId, ownerId);

            // Find group
            Group group = findGroupById(groupId);

            // Check ownership
            if (group.getOwner() == null || !group.getOwner().getId().equals(ownerId)) {
                throw new IllegalStateException("Only group owner can delete the group");
            }

            // Delete all memberships first
            membershipRepository.deleteByGroupId(groupId);

            // Delete all posts
            postRepository.deleteByGroupId(groupId);

            // Delete the group
            groupRepository.delete(group);

            logger.info("Group {} deleted successfully", groupId);

        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Error deleting group: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error deleting group: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete group", e);
        }
    }

    /**
     * Get group details by ID
     */
    @Transactional(readOnly = true)
    public GroupResponse getGroupById(Long groupId, Long userId) {
        logger.info("Getting group details for group: {}, user: {}", groupId, userId);

        try {
            // Input validation
            if (groupId == null) {
                throw new IllegalArgumentException("Group ID cannot be null");
            }

            // Find group
            Group group = findGroupById(groupId);

            // Validate group data
            if (group.getOwner() == null) {
                logger.error("Group {} has null owner", groupId);
                throw new IllegalStateException("Group data is corrupted - missing owner");
            }

            // Create response
            GroupResponse response = new GroupResponse();
            response.setId(group.getId());
            response.setName(group.getName());
            response.setDescription(group.getDescription());
            response.setPrivacy(group.getPrivacy());
            response.setOwnerId(group.getOwner().getId());
            response.setOwnerName(group.getOwner().getUsername());
            response.setCreatedAt(group.getCreatedAt());
            response.setCoverImage(group.getCoverImage());
            response.setMemberCount(group.getMemberCount());
            response.setPostCount(group.getPostCount());

            // Set membership info if userId provided
            if (userId != null) {
                boolean isMember = isMember(groupId, userId);
                response.setIsMember(isMember);

                if (isMember) {
                    Optional<GroupMembership.Role> role = getRole(groupId, userId);
                    response.setUserRole(role.map(Enum::name).orElse(null));
                } else {
                    response.setUserRole(null);
                }
            } else {
                response.setIsMember(false);
                response.setUserRole(null);
            }

            return response;

        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Error getting group details: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error getting group details: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get group details", e);
        }
    }

    /**
     * Get all groups for a user
     */
    @Transactional(readOnly = true)
    public List<GroupResponse> getGroupsByUserId(Long userId) {
        logger.info("Getting groups for user: {}", userId);

        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }

            List<GroupMembership> memberships = membershipRepository.findByUserId(userId);

            return memberships.stream()
                    .filter(membership -> membership.getGroup() != null)
                    .map(membership -> {
                        try {
                            return getGroupById(membership.getGroup().getId(), userId);
                        } catch (Exception e) {
                            logger.error("Error processing group for user {}: {}", userId, e.getMessage());
                            return null;
                        }
                    })
                    .filter(response -> response != null)
                    .collect(Collectors.toList());

        } catch (IllegalArgumentException e) {
            logger.error("Error getting user groups: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error getting user groups: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Search groups
     */
    @Transactional(readOnly = true)
    public List<GroupResponse> searchGroups(String searchTerm, Long userId) {
        logger.info("Searching groups with term: '{}' for user: {}", searchTerm, userId);

        try {
            List<Group> groups;

            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                // Return all public groups
                groups = groupRepository.findByPrivacy(Group.Privacy.PUBLIC);
            } else {
                // Search by name or description
                groups = groupRepository.searchByNameOrDescriptionAndPrivacy(searchTerm.trim(), Group.Privacy.PUBLIC);
            }

            return groups.stream()
                    .filter(group -> group != null && group.getOwner() != null)
                    .map(group -> {
                        try {
                            return getGroupById(group.getId(), userId);
                        } catch (Exception e) {
                            logger.error("Error processing search result for group {}: {}", group.getId(),
                                    e.getMessage());
                            return null;
                        }
                    })
                    .filter(response -> response != null)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            logger.error("Error searching groups: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get group members
     */
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getGroupMembers(Long groupId, Long userId) {
        logger.info("Getting members for group: {} by user: {}", groupId, userId);

        try {
            // Input validation
            validateIds(groupId, userId);

            // Check if user is a member
            if (!isMember(groupId, userId)) {
                throw new IllegalStateException("User is not a member of this group");
            }

            List<GroupMembership> memberships = membershipRepository.findByGroupId(groupId);

            return memberships.stream()
                    .filter(membership -> membership.getUser() != null)
                    .map(membership -> {
                        try {
                            GroupMemberResponse response = new GroupMemberResponse();
                            response.setUserId(membership.getUser().getId());
                            response.setUserName(membership.getUser().getUsername());
                            response.setUserEmail(membership.getUser().getEmail());
                            response.setRole(membership.getRole());
                            response.setJoinedAt(membership.getJoinedAt());
                            response.setProfileImage(membership.getUser().getProfilePictureUrl());
                            return response;
                        } catch (Exception e) {
                            logger.error("Error processing member: {}", e.getMessage());
                            return null;
                        }
                    })
                    .filter(response -> response != null)
                    .collect(Collectors.toList());

        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.error("Error getting group members: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error getting group members: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get group members", e);
        }
    }

    /**
     * Check if user is a member of the group
     */
    @Transactional(readOnly = true)
    public boolean isMember(Long groupId, Long userId) {
        if (groupId == null || userId == null) {
            return false;
        }
        try {
            return membershipRepository.existsByGroupIdAndUserId(groupId, userId);
        } catch (Exception e) {
            logger.error("Error checking membership for group {} and user {}: {}", groupId, userId, e.getMessage());
            return false;
        }
    }

    /**
     * Get user's role in the group
     */
    @Transactional(readOnly = true)
    public Optional<GroupMembership.Role> getRole(Long groupId, Long userId) {
        if (groupId == null || userId == null) {
            return Optional.empty();
        }
        try {
            return membershipRepository.findByGroupIdAndUserId(groupId, userId)
                    .map(GroupMembership::getRole);
        } catch (Exception e) {
            logger.error("Error getting role for group {} and user {}: {}", groupId, userId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Check if user can post in the group
     */
    @Transactional(readOnly = true)
    public boolean canPostInGroup(Long groupId, Long userId) {
        if (groupId == null || userId == null) {
            return false;
        }

        try {
            if (!isMember(groupId, userId)) {
                return false;
            }

            Optional<GroupMembership.Role> role = getRole(groupId, userId);
            return role.isPresent() && role.get() != GroupMembership.Role.BANNED;
        } catch (Exception e) {
            logger.error("Error checking post permission for group {} and user {}: {}", groupId, userId,
                    e.getMessage());
            return false;
        }
    }

    // Helper methods

    private void validateIds(Long groupId, Long userId) {
        if (groupId == null || userId == null) {
            throw new IllegalArgumentException("Group ID and User ID cannot be null");
        }
    }

    private Group findGroupById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));
    }

    private void validateAdminPermissions(Long groupId, Long adminUserId) {
        GroupMembership adminMembership = membershipRepository.findByGroupIdAndUserId(groupId, adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user is not a member of this group"));

        if (adminMembership.getRole() != GroupMembership.Role.OWNER &&
                adminMembership.getRole() != GroupMembership.Role.ADMIN) {
            throw new IllegalStateException("Only owners and admins can perform this action");
        }
    }
}
