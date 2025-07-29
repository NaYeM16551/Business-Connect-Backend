package com.example.demo.repository.Groups;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Groups.Group;
import com.example.demo.model.Groups.Group.Privacy;

public interface GroupRepository extends JpaRepository<Group, Long> {
        List<Group> findByPrivacy(Privacy privacy);

        // Search groups by name or description (case-insensitive)
        @Query("SELECT g FROM Group g WHERE " +
                        "(LOWER(g.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(g.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
                        "g.privacy = :privacy")
        List<Group> searchByNameOrDescriptionAndPrivacy(@Param("searchTerm") String searchTerm,
                        @Param("privacy") Privacy privacy);

        // Search all groups by name or description (case-insensitive)
        @Query("SELECT g FROM Group g WHERE " +
                        "LOWER(g.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
                        "LOWER(g.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
        List<Group> searchByNameOrDescription(@Param("searchTerm") String searchTerm);

        // Search groups by types
        @Query("SELECT g FROM Group g WHERE LOWER(g.type) IN :groupTypes")
        List<Group> searchByGroupTypes(@Param("groupTypes") List<String> groupTypes);
}
