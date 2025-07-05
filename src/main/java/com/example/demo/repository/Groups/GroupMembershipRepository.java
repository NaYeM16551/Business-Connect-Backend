package com.example.demo.repository.Groups;
import com.example.demo.model.Groups.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, GroupMembershipId> {
    List<GroupMembership> findByGroupId(Long groupId);
    List<GroupMembership> findByUserId(Long userId);
    // For quick membership check:
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
    
    @Modifying
    @Query("DELETE FROM GroupMembership gm WHERE gm.group.id = :groupId")
    void deleteByGroupId(@Param("groupId") Long groupId);
}
