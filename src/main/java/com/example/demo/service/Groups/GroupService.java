package com.example.demo.service.Groups;

import com.example.demo.model.Groups.*;
import com.example.demo.model.User;
import com.example.demo.repository.Groups.*;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepo;
    @Autowired
    private GroupMembershipRepository membershipRepo;
    @Autowired
    private UserRepository userRepo;


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

        // If CLOSED or PRIVATE, you’d record a “join request” in a separate table
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

    public boolean isMember(Long groupId, Long userId) {
        return membershipRepo.existsByGroupIdAndUserId(groupId, userId);
    }

    public Optional<GroupMembership.Role> getRole(Long groupId, Long userId) {
        return membershipRepo.findById(new GroupMembershipId(groupId, userId))
                             .map(GroupMembership::getRole);
    }

    // Additional methods: leaveGroup, renameGroup, changePrivacy, promote/demote roles, banUser...
}
