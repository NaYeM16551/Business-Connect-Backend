package com.example.demo.Contest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<SubmissionModel, Long> {

        @Query(value = "SELECT * FROM submissions WHERE contest_id = ?1 AND participant_id = ?2", nativeQuery = true)
        List<SubmissionModel> findSubmissionsByContestIdAndParticipantId(Long contestId, Long participantId);

        @Query(value = "SELECT * FROM submissions WHERE contest_id = ?1", nativeQuery = true)
        List<SubmissionModel> findAllByContestId(Long contestId);
}