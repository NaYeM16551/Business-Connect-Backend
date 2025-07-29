package com.example.demo.repository.Groups;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Groups.GroupMembership;
import com.example.demo.model.Groups.GroupMembershipId;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, GroupMembershipId> {
    List<GroupMembership> findByGroupId(Long groupId);

    List<GroupMembership> findByUserId(Long userId);

    // For quick membership check:
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    // Find specific membership by group and user ID
    @Query("SELECT gm FROM GroupMembership gm WHERE gm.group.id = :groupId AND gm.user.id = :userId")
    Optional<GroupMembership> findByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM GroupMembership gm WHERE gm.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);
}
