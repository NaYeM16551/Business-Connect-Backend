package com.example.demo.Contest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.User;
import com.example.demo.service.CloudinaryService;

@RestController
@RequestMapping("/api/v1/{id}")
public class ContestController {
    @Autowired
    private ContestService contestService;
    @Autowired
    private CloudinaryService cloudinaryService;

    @PostMapping("/contests/create")
    public ResponseEntity<ContestModel> createContest(@RequestBody ContestModel contest) {
        ContestModel createdContest = contestService.createContest(contest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdContest);
    }
    @GetMapping("/contests/myContests")
    public ResponseEntity<List<ContestModel>> getMyContests(@PathVariable Long id) {
        List<ContestModel> myContests = contestService.getMyContests(id);
        return ResponseEntity.ok(myContests);
    }

    @GetMapping("/contests/{contestId}")
    public ResponseEntity<ContestModel> getContestById(@PathVariable Long contestId) {
        try {
            ContestModel contest = contestService.getContestById(contestId);
            return ResponseEntity.ok(contest);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/contests")
    public ResponseEntity<List<ContestModel>> getAllContests() {
        List<ContestModel> contests = contestService.getAllContests();
        return ResponseEntity.ok(contests);
    }

    @GetMapping("/contests/LiveContests")
    public ResponseEntity<List<ContestModel>> getLiveContests() {
        System.out.println("Fetching live contests");
        List<ContestModel> liveContests = contestService.getLiveContests();
        return ResponseEntity.ok(liveContests);
    }

    @GetMapping("/contests/UpcomingContests")
    public ResponseEntity<List<ContestModel>> getUpcomingContests() {
        // This method should be implemented to fetch upcoming contests based on the provided date.
        // For now, it returns an empty list as a placeholder.
        //cout<<""
        List<ContestModel> upcomingContests = contestService.getUpcomingContests();
        return ResponseEntity.ok(upcomingContests);
    }

    @GetMapping("/contests/PastContests")
    public ResponseEntity<List<ContestModel>> getPastContests() {
        List<ContestModel> pastContests = contestService.getPastContests();
        return ResponseEntity.ok(pastContests);
    }

    @GetMapping("/contests/LiveContests/{contestId}")
    public ResponseEntity<List<QuestionModel>> getQuestionsofLiveContestById(@PathVariable Long contestId) {
        try {
            List<QuestionModel> questions = contestService.getQuestionsofLiveContest(contestId);
            return ResponseEntity.ok(questions);
        }
        catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/contests/{contestId}/start")
    public ResponseEntity<Void> startContest(@PathVariable Long contestId) throws ResourceNotFoundException {
        contestService.startContest(contestId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/contests/{contestId}/finish")
    public ResponseEntity<Void> finishContest(@PathVariable Long contestId) {
        try {
            System.out.println("Finishing contest with ID: " + contestId);
            contestService.finishContest(contestId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/contests/{contestId}/MakeQuestion")
    public ResponseEntity<Void> makeQuestion(@PathVariable Long contestId, @RequestBody QuestionModel question) {
        try {
            contestService.makeQuestion(contestId, question);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
        catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/contests/{contestId}/GetAllQuestions")
    public ResponseEntity<List<QuestionModel>> getAllquestions(@PathVariable Long contestId) {
        try {
            return ResponseEntity.ok(contestService.getQuestionsofLiveContest(contestId));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/contests/{contestId}/join")
    public ResponseEntity<Void> joinContest(@PathVariable Long contestId, @RequestBody ParticipantModel participant) {
        // Assuming you have a method to handle joining a contest
        // This is a placeholder implementation
        try {
            contestService.joinContest(participant);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/contests/{contestId}/participants")
    public ResponseEntity<List<ParticipantModel>> getParticipantsByContestId(@PathVariable Long contestId) {
        List<ParticipantModel> participants = contestService.getParticipantsByContestId(contestId);
        return ResponseEntity.ok(participants);
    }

    @GetMapping("/contests/{contestId}/questions")
    public ResponseEntity<List<QuestionModel>> getQuestionsByContestId(@PathVariable Long contestId) {
        List<QuestionModel> questions = null;
        try {
            questions = contestService.getQuestionsofLiveContest(contestId);
        } catch (ResourceNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ResponseEntity.ok(questions);
    }

    // @PostMapping(value = "/contests/{contestId}/questions/{questionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    // public ResponseEntity<Void> uploadFile(@PathVariable Long contestId, @RequestParam("file") MultipartFile file, @PathVariable Long questionId, @PathVariable Long id) {
    //     try {
    //         contestService.uploadFile(contestId, file, questionId, id);
    //         return ResponseEntity.status(HttpStatus.CREATED).build();
    //     } catch (Exception e) {
    //         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    //     }
    // }

    @PostMapping(value = "/contests/{contestId}/questions/{questionId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public ResponseEntity<Map<String, Object>> uploadFile(
        @PathVariable Long contestId,
        @RequestParam("file") MultipartFile file,
        @PathVariable Long questionId,
        @PathVariable Long id) {
    try {
        Map<String, Object> response = new HashMap<>();
        //String fileUrl = "http://localhost:8080/uploads/" + submission.getFileUrl();
        String fileUrl = cloudinaryService.uploadFile(file);
        //submission.setFileUrl(fileUrl);
        SubmissionModel submission = contestService.uploadFile(contestId, file, questionId, id, fileUrl);

        response.put("submissionId", submission.getId());
        response.put("fileUrl", fileUrl);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}


    @GetMapping("/contests/{contestId}/submissions")
    public ResponseEntity<List<SubmissionModel>> getSubmissionsByContestIdAndParticipantId(@PathVariable Long contestId, @PathVariable Long id) {
        List<SubmissionModel> submissions = contestService.getSubmissionsByContestIdandParticipantId(contestId, id);
        return ResponseEntity.ok(submissions);
    }

    
    @DeleteMapping("/contests/{contestId}/submissions/{submissionId}")
    public ResponseEntity<Void> deleteSubmission(@PathVariable Long contestId, @PathVariable Long submissionId) {
        try {
            contestService.deleteSubmission(submissionId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/contests/{contestId}/allsubmissions")
    public ResponseEntity<List<SubmissionModel>> getAllSubmissionsByContestId(@PathVariable Long contestId) {
        List<SubmissionModel> submissions = contestService.getAllSubmissionsByContestId(contestId);
        return ResponseEntity.ok(submissions);
    }

    @PutMapping("/contests/{contestId}/allsubmissions/{submissionId}/grade")
    public ResponseEntity<Void> gradeSubmission(@PathVariable Long contestId, @PathVariable Long submissionId, @RequestParam Long grade) {
        try {
            contestService.gradeSubmission(submissionId, grade);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/contests/{contestId}/leaderboard")
    public ResponseEntity<List<LeaderBoardDto>> getContestLeaderboard(@PathVariable Long contestId) {
        List<LeaderBoardDto> leaderboard = contestService.getContestLeaderboard(contestId);
        System.out.println(leaderboard);
        return ResponseEntity.ok(leaderboard);
    }
}