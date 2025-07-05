package com.example.demo.service.Groups;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.Groups.GroupMemberResponse;
import com.example.demo.dto.Groups.GroupResponse;
import com.example.demo.model.User;
import com.example.demo.model.Groups.Group;
import com.example.demo.model.Groups.GroupMembership;
import com.example.demo.model.Groups.GroupMembershipId;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.Groups.GroupMembershipRepository;
import com.example.demo.repository.Groups.GroupRepository;
import com.example.demo.repository.Posts.PostRepository;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepo;
    @Autowired
    private GroupMembershipRepository membershipRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private PostRepository postRepo;

    @Transactional
    public Group createGroup(String name, String description,
            Group.Privacy privacy, Long ownerId) {
        User owner = userRepo.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Group group = new Group();
        group.setName(name);
        group.setDescription(description);
        group.setPrivacy(privacy);
        group.setOwner(owner);
        group = groupRepo.save(group);

        // Add owner as OWNER in memberships
        GroupMembership gm = new GroupMembership();
        gm.setGroup(group);
        gm.setUser(owner);
        gm.setRole(GroupMembership.Role.OWNER);
        membershipRepo.save(gm);

        // Increment member count
        group.setMemberCount(1);
        groupRepo.save(group);

        return group;
    }

    @Transactional
    public void joinGroup(Long groupId, Long userId) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        // If already a member or banned, do nothing / or throw
        if (membershipRepo.existsByGroupIdAndUserId(groupId, userId)) {
            return;
        }

        // If privacy = PUBLIC, auto-approve
        GroupMembership.Role newRole = GroupMembership.Role.MEMBER;

        // If CLOSED or PRIVATE, you'd record a "join request" in a separate table
        // and inform moderators/admins. For brevity, we auto-approve for PUBLIC only.
        if (group.getPrivacy() != Group.Privacy.PUBLIC) {
            throw new IllegalStateException("Cannot auto-join a closed or private group");
        }

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        GroupMembership gm = new GroupMembership();
        gm.setGroup(group);
        gm.setUser(user);
        gm.setRole(newRole);
        membershipRepo.save(gm);

        // Increment group.memberCount
        group.setMemberCount(group.getMemberCount() + 1);
        groupRepo.save(group);
    }

    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        GroupMembership membership = membershipRepo.findById(new GroupMembershipId(groupId, userId))
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this group"));

        // Owner cannot leave the group
        if (membership.getRole() == GroupMembership.Role.OWNER) {
            throw new IllegalStateException("Group owner cannot leave the group");
        }

        membershipRepo.delete(membership);

        // Decrement member count
        group.setMemberCount(group.getMemberCount() - 1);
        groupRepo.save(group);
    }

    @Transactional
    public void updateGroupRole(Long groupId, Long userId, Long adminUserId, GroupMembership.Role newRole) {
        // Check if admin user has permission to change roles
        GroupMembership adminMembership = membershipRepo.findById(new GroupMembershipId(groupId, adminUserId))
                .orElseThrow(() -> new IllegalArgumentException("Admin user is not a member of this group"));

        if (adminMembership.getRole() != GroupMembership.Role.OWNER
                && adminMembership.getRole() != GroupMembership.Role.ADMIN) {
            throw new IllegalStateException("Only owners and admins can change member roles");
        }

        GroupMembership membership = membershipRepo.findById(new GroupMembershipId(groupId, userId))
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this group"));

        // Cannot change owner's role
        if (membership.getRole() == GroupMembership.Role.OWNER) {
            throw new IllegalStateException("Cannot change owner's role");
        }

        membership.setRole(newRole);
        membershipRepo.save(membership);
    }

    @Transactional
    public void removeMember(Long groupId, Long userId, Long adminUserId) {
        // Check if admin user has permission
        GroupMembership adminMembership = membershipRepo.findById(new GroupMembershipId(groupId, adminUserId))
                .orElseThrow(() -> new IllegalArgumentException("Admin user is not a member of this group"));

        if (adminMembership.getRole() != GroupMembership.Role.OWNER
                && adminMembership.getRole() != GroupMembership.Role.ADMIN) {
            throw new IllegalStateException("Only owners and admins can remove members");
        }

        GroupMembership membership = membershipRepo.findById(new GroupMembershipId(groupId, userId))
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this group"));

        // Cannot remove owner
        if (membership.getRole() == GroupMembership.Role.OWNER) {
            throw new IllegalStateException("Cannot remove group owner");
        }

        membershipRepo.delete(membership);

        // Decrement member count
        Group group = groupRepo.findById(groupId).orElseThrow();
        group.setMemberCount(group.getMemberCount() - 1);
        groupRepo.save(group);
    }

    @Transactional
    public void updateGroup(Long groupId, Long adminUserId, String name, String description, Group.Privacy privacy,
            String coverImage) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        // Check if user has permission to update group
        GroupMembership membership = membershipRepo.findById(new GroupMembershipId(groupId, adminUserId))
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this group"));

        if (membership.getRole() != GroupMembership.Role.OWNER && membership.getRole() != GroupMembership.Role.ADMIN) {
            throw new IllegalStateException("Only owners and admins can update group settings");
        }

        if (name != null)
            group.setName(name);
        if (description != null)
            group.setDescription(description);
        if (privacy != null)
            group.setPrivacy(privacy);
        if (coverImage != null)
            group.setCoverImage(coverImage);

        groupRepo.save(group);
    }

    @Transactional
    public void deleteGroup(Long groupId, Long ownerId) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        // Only owner can delete group
        if (!group.getOwner().getId().equals(ownerId)) {
            throw new IllegalStateException("Only group owner can delete the group");
        }

        // Delete all memberships
        membershipRepo.deleteByGroupId(groupId);

        // Delete all group posts
        postRepo.deleteByGroupId(groupId);

        // Delete the group
        groupRepo.delete(group);
    }

    public boolean isMember(Long groupId, Long userId) {
        return membershipRepo.existsByGroupIdAndUserId(groupId, userId);
    }

    public Optional<GroupMembership.Role> getRole(Long groupId, Long userId) {
        return membershipRepo.findById(new GroupMembershipId(groupId, userId))
                .map(GroupMembership::getRole);
    }

    public GroupResponse getGroupById(Long groupId, Long userId) {
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

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

        // Check if user is a member
        boolean isMember = isMember(groupId, userId);
        response.setIsMember(isMember);

        if (isMember) {
            Optional<GroupMembership.Role> role = getRole(groupId, userId);
            response.setUserRole(role.map(Enum::name).orElse(null));
        }

        return response;
    }

    public List<GroupResponse> getGroupsByUserId(Long userId) {
        List<GroupMembership> memberships = membershipRepo.findByUserId(userId);
        return memberships.stream()
                .map(membership -> getGroupById(membership.getGroup().getId(), userId))
                .collect(Collectors.toList());
    }

    public List<GroupResponse> searchGroups(String searchTerm, Long userId) {
        // This would need a custom query in GroupRepository
        // For now, returning all public groups
        List<Group> groups = groupRepo.findByPrivacy(Group.Privacy.PUBLIC);
        return groups.stream()
                .map(group -> getGroupById(group.getId(), userId))
                .collect(Collectors.toList());
    }

    public List<GroupMemberResponse> getGroupMembers(Long groupId, Long userId) {
        // Check if user is a member
        if (!isMember(groupId, userId)) {
            throw new IllegalStateException("User is not a member of this group");
        }

        List<GroupMembership> memberships = membershipRepo.findByGroupId(groupId);
        return memberships.stream()
                .map(membership -> {
                    GroupMemberResponse response = new GroupMemberResponse();
                    response.setUserId(membership.getUser().getId());
                    response.setUserName(membership.getUser().getUsername());
                    response.setUserEmail(membership.getUser().getEmail());
                    response.setRole(membership.getRole());
                    response.setJoinedAt(membership.getJoinedAt());
                    response.setProfileImage(membership.getUser().getProfilePictureUrl());
                    return response;
                })
                .collect(Collectors.toList());
    }

    public boolean canPostInGroup(Long groupId, Long userId) {
        if (!isMember(groupId, userId)) {
            return false;
        }

        Optional<GroupMembership.Role> role = getRole(groupId, userId);
        return role.isPresent() && role.get() != GroupMembership.Role.BANNED;
    }

    // Additional methods: leaveGroup, renameGroup, changePrivacy, promote/demote
    // roles, banUser...
}
