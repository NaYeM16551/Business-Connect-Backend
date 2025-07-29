package com.example.demo.Contest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import jakarta.transaction.TransactionScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@Repository
public interface ContestRepository extends JpaRepository<ContestModel, Long> {
    
    @Query(value = "SELECT * FROM contests  WHERE status = 'LIVE'", nativeQuery = true)
    List<ContestModel> findLive();
   
    @Query(value = "SELECT * FROM contests WHERE status = 'DECLARED'", nativeQuery = true)
    List<ContestModel> findUpcoming();

    @Query(value = "SELECT * FROM contests WHERE status = 'PAST'", nativeQuery = true)
    List<ContestModel> findPast();

    @Transactional
    @Modifying
    @Query(value = "UPDATE contests SET status = 'LIVE' WHERE id = ?1", nativeQuery = true)
    void StartContest(Long contestId);
    
    @Transactional
    @Modifying
    @Query(value = "UPDATE contests SET status = 'PAST' WHERE id = ?1", nativeQuery = true)
    void FinishContest(Long contestId);

    @Query(value = "SELECT * FROM contests WHERE created_by = ?1", nativeQuery = true)
    List<ContestModel> getContestsByUserId(Long userId);
    
    @Query(value = "SELECT   u.username,  s.participant_id,  SUM(s.grade) AS total_grade\n" + //
                "FROM submissions s JOIN  users u ON s.participant_id = u.id\n" + //
                "WHERE  s.contest_id = ?1\n" + //
                "GROUP BY  u.username, s.participant_id\n" + //
                "ORDER BY   total_grade DESC;", nativeQuery = true)
    List<Object[]> getContestLeaderboard(Long contestId);
}
