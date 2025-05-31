package com.example.demo.repository.Follow_Unfollow;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.model.Follow_Unfollow.Follow;

public interface FollowUnfollowRepository extends JpaRepository<Follow, Long> {

    /**
     * Find the single Follow record where follower.id = :followerId AND followee.id
     * = :followeeId.
     */
    @Query("SELECT f FROM Follow f WHERE f.follower.id = :followerId AND f.followee.id = :followeeId")
    Optional<Follow> findByFollowerAndFollowee(
            @Param("followerId") Long followerId,
            @Param("followeeId") Long followeeId);

    /**
     * Find all Follow rows where follower.id = :followerId.
     */
    @Query("SELECT f FROM Follow f WHERE f.follower.id = :followerId")
    List<Follow> findByFollowerId(@Param("followerId") Long followerId);

    /**
     * Find all Follow rows where followee.id = :followeeId.
     */
    @Query("SELECT f FROM Follow f WHERE f.followee.id = :followeeId")
    List<Follow> findByFolloweeId(@Param("followeeId") Long followeeId);

    /**
     * Delete the Follow row where follower.id = :followerId AND followee.id =
     * :followeeId.
     * Because this is a bulk‐delete JPQL, annotate with @Modifying (and invoke from
     * a @Transactional context).
     */
    @Modifying
    @Query("DELETE FROM Follow f WHERE f.follower.id = :followerId AND f.followee.id = :followeeId")
    void deleteByFollowerIdAndFolloweeId(
            @Param("followerId") Long followerId,
            @Param("followeeId") Long followeeId);

    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    // a method to delete rows that have followerID or followeeID as the given ID
    @Modifying
    @Query("DELETE FROM Follow f WHERE f.follower.id = :userId OR f.followee.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
