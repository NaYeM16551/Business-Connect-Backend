package com.example.demo.model.Groups;

import java.io.Serializable;
import java.util.Objects;

public class GroupMembershipId implements Serializable {
    private Long group;
    private Long user;

    public GroupMembershipId() {}

    public GroupMembershipId(Long group, Long user) {
        this.group = group;
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupMembershipId that = (GroupMembershipId) o;
        return Objects.equals(group, that.group) &&
               Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, user);
    }
}
