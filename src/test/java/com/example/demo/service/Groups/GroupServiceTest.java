package com.example.demo.service.Groups;

import com.example.demo.model.Groups.Group;
import com.example.demo.model.Groups.GroupMembership;
import com.example.demo.model.User;
import com.example.demo.repository.Groups.GroupMembershipRepository;
import com.example.demo.repository.Groups.GroupRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GroupServiceTest {

    @Mock
    private GroupRepository groupRepo;

    @Mock
    private GroupMembershipRepository membershipRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private GroupService groupService;

    private User testUser;
    private Group testGroup;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        
        // Setup test group
        testGroup = new Group();
        testGroup.setId(1L);
        testGroup.setName("Test Group");
        testGroup.setDescription("Test Description");
        testGroup.setPrivacy(Group.Privacy.PUBLIC);
        testGroup.setOwner(testUser);
        testGroup.setMemberCount(1);
        testGroup.setPostCount(0);
    }

    @Test
    void testCreateGroup() {
        // Given
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(groupRepo.save(any(Group.class))).thenReturn(testGroup);
        when(membershipRepo.save(any(GroupMembership.class))).thenReturn(new GroupMembership());

        // When
        Group result = groupService.createGroup("Test Group", "Test Description", Group.Privacy.PUBLIC, 1L);

        // Then
        assertNotNull(result);
        assertEquals("Test Group", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(Group.Privacy.PUBLIC, result.getPrivacy());
        assertEquals(testUser, result.getOwner());
        assertEquals(1, result.getMemberCount());

        verify(groupRepo, times(2)).save(any(Group.class));
        verify(membershipRepo, times(1)).save(any(GroupMembership.class));
    }

    @Test
    void testJoinGroup() {
        // Given
        when(groupRepo.findById(1L)).thenReturn(Optional.of(testGroup));
        when(userRepo.findById(1L)).thenReturn(Optional.of(testUser));
        when(membershipRepo.existsByGroupIdAndUserId(1L, 1L)).thenReturn(false);
        when(membershipRepo.save(any(GroupMembership.class))).thenReturn(new GroupMembership());

        // When
        groupService.joinGroup(1L, 1L);

        // Then
        verify(membershipRepo, times(1)).save(any(GroupMembership.class));
        verify(groupRepo, times(1)).save(any(Group.class));
    }

    @Test
    void testIsMember() {
        // Given
        when(membershipRepo.existsByGroupIdAndUserId(1L, 1L)).thenReturn(true);

        // When
        boolean result = groupService.isMember(1L, 1L);

        // Then
        assertTrue(result);
        verify(membershipRepo, times(1)).existsByGroupIdAndUserId(1L, 1L);
    }

    @Test
    void testGetRole() {
        // Given
        GroupMembership membership = new GroupMembership();
        membership.setRole(GroupMembership.Role.MEMBER);
        when(membershipRepo.findById(any())).thenReturn(Optional.of(membership));

        // When
        Optional<GroupMembership.Role> result = groupService.getRole(1L, 1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(GroupMembership.Role.MEMBER, result.get());
    }

    @Test
    void testCanPostInGroup() {
        // Given
        when(membershipRepo.existsByGroupIdAndUserId(1L, 1L)).thenReturn(true);
        GroupMembership membership = new GroupMembership();
        membership.setRole(GroupMembership.Role.MEMBER);
        when(membershipRepo.findById(any())).thenReturn(Optional.of(membership));

        // When
        boolean result = groupService.canPostInGroup(1L, 1L);

        // Then
        assertTrue(result);
    }

    @Test
    void testCannotPostInGroupWhenNotMember() {
        // Given
        when(membershipRepo.existsByGroupIdAndUserId(1L, 1L)).thenReturn(false);

        // When
        boolean result = groupService.canPostInGroup(1L, 1L);

        // Then
        assertFalse(result);
    }

    @Test
    void testCannotPostInGroupWhenBanned() {
        // Given
        when(membershipRepo.existsByGroupIdAndUserId(1L, 1L)).thenReturn(true);
        GroupMembership membership = new GroupMembership();
        membership.setRole(GroupMembership.Role.BANNED);
        when(membershipRepo.findById(any())).thenReturn(Optional.of(membership));

        // When
        boolean result = groupService.canPostInGroup(1L, 1L);

        // Then
        assertFalse(result);
    }
} 