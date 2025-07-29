package com.example.demo.Contest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<QuestionModel, Long> {
 
    @Query(value = "SELECT * FROM questions  WHERE contest_id = ?1", nativeQuery = true)
    List<QuestionModel> findQuestionsByContestId(Long contestId);


}
