package com.example.demo.Contest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ParticipantRepository extends JpaRepository<ParticipantModel, Long> {

    @Query(value = "SELECT * FROM participants WHERE contest_id = ?1", nativeQuery = true)
    List<ParticipantModel> findParticipantsByContestId(Long contestId);

    @Query(value = "SELECT * FROM participants WHERE user_id = ?1 AND contest_id = ?2", nativeQuery = true)
    ParticipantModel findByUserIdAndContestId(Long userId, Long contestId);

    
}
