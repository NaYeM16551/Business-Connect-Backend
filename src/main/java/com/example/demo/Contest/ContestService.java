package com.example.demo.Contest;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.example.demo.Contest.ContestModel;
import com.example.demo.Contest.ContestRepository;
import com.example.demo.Contest.QuestionModel;
import com.example.demo.Contest.QuestionRepository;
import com.example.demo.Contest.ParticipantModel;
import com.example.demo.Contest.ParticipantRepository;
// This class is a placeholder for the ContestService implementation.
// It will contain methods to handle business logic related to contests,
// such as creating contests, managing participants, and handling submissions.

@Service
public class ContestService {
    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ParticipantRepository participantRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    public ContestModel createContest(ContestModel contest) {
        contest.setStatus("DECLARED"); // Default status when creating a contest
        return contestRepository.save(contest);
    }

    public ContestModel getContestById(Long contestId) throws ResourceNotFoundException {
        return contestRepository.findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Contest not found for this id :: " + contestId));
    }

    public List<ContestModel> getAllContests() {
        return contestRepository.findAll();
    }

    public List<ContestModel> getLiveContests() {
        return contestRepository.findLive();
    }

    public List<ContestModel> getUpcomingContests() {
        // This method should be implemented to fetch upcoming contests based on the provided date.
        // For now, it returns an empty list as a placeholder.
        return contestRepository.findUpcoming();
    }
    public List<ContestModel> getPastContests() {
        // This method should be implemented to fetch upcoming contests based on the provided date.
        // For now, it returns an empty list as a placeholder.
        return contestRepository.findPast();
    }

    public List<QuestionModel> getQuestionsofLiveContest(Long contestId) throws ResourceNotFoundException {
        List<QuestionModel> questions = questionRepository.findQuestionsByContestId(contestId);
        if (questions.isEmpty()) {
            throw new ResourceNotFoundException("No questions found for this contest id :: " + contestId);
        }
        return questions;
    }

    public void startContest(Long contestId) throws ResourceNotFoundException {
        contestRepository.StartContest(contestId);
        if (!contestRepository.existsById(contestId)) {
            throw new ResourceNotFoundException("Contest not found for this id :: " + contestId);
        }
    }

    public void finishContest(Long contestId) throws ResourceNotFoundException {
        contestRepository.FinishContest(contestId);
        if (!contestRepository.existsById(contestId)) {
            throw new ResourceNotFoundException("Contest not found for this id :: " + contestId);
        }
    }

    public void makeQuestion(Long contestId, QuestionModel question) throws ResourceNotFoundException {
        if (!contestRepository.existsById(contestId)) {
            throw new ResourceNotFoundException("Contest not found for this id :: " + contestId);
        }
         questionRepository.save(question);
    }

    public void joinContest(ParticipantModel participant) throws ResourceNotFoundException {
        // Logic to add participant to the contest
        if (!contestRepository.existsById(participant.getContestId())) {
            throw new ResourceNotFoundException("Contest not found for this id :: " + participant.getContestId());
        }
        // Save the participant to the database
         participantRepository.save(participant);
    }

    public List<ParticipantModel> getParticipantsByContestId(Long contestId) {
        return participantRepository.findParticipantsByContestId(contestId);
    }

    public SubmissionModel uploadFile(Long contestId, MultipartFile file, Long questionId, Long participantId, String fileUrl) throws ResourceNotFoundException {
        SubmissionModel submission = SubmissionModel.builder()
                .contestId(contestId)
                .questionId(questionId)
                .participantId(participantId)
                .fileUrl(fileUrl)
                .submittedAt(LocalDateTime.now())
                .status("SUBMITTED")
                .build();

        return submissionRepository.save(submission);

        // Logic to handle file upload
    }
    public List<SubmissionModel> getSubmissionsByContestIdandParticipantId(Long contestId, Long participantId) {
        return submissionRepository.findSubmissionsByContestIdAndParticipantId(contestId, participantId);
    }

    public List<SubmissionModel> getAllSubmissionsByContestId(Long contestId) {
        return submissionRepository.findAllByContestId(contestId);
    }

    public void deleteSubmission(Long submissionId) throws ResourceNotFoundException {
        if (!submissionRepository.existsById(submissionId)) {
            throw new ResourceNotFoundException("Submission not found for this id :: " + submissionId);
        }
        submissionRepository.deleteById(submissionId);
    }

    public void gradeSubmission(Long submissionId, Long grade) throws ResourceNotFoundException {
        SubmissionModel submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission not found for this id :: " + submissionId));
        submission.setGrade(grade);
        submission.setStatus("GRADED");
        submissionRepository.save(submission);
    }

    public List<ContestModel> getMyContests(Long userId) {
        // This method should be implemented to fetch contests created by the user.
        // For now, it returns an empty list as a placeholder.
        return contestRepository.getContestsByUserId(userId);
    }
    public List<LeaderBoardDto> getContestLeaderboard(Long contestId) {
        // This method should be implemented to fetch the leaderboard for the contest.
        // For now, it returns an empty list as a placeholder.
        List<Object[]> results = contestRepository.getContestLeaderboard(contestId);
        return results.stream()
            .map(r -> new LeaderBoardDto(
                (String) r[0],
                ((Number) r[1]).longValue(),
                ((Number) r[2]).longValue()
            ))
            .collect(Collectors.toList());
        //return contestRepository.getContestLeaderboard(contestId);
    }

}