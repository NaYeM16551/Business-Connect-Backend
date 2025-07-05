package com.example.demo.repository.Groups;

import com.example.demo.model.Groups.Group;
import com.example.demo.model.Groups.Group.Privacy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByPrivacy(Privacy privacy);
}

