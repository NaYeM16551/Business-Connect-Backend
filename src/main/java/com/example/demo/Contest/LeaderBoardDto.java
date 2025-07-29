package com.example.demo.Contest;

public class LeaderBoardDto {
    public Long participant_id;
    public String userName;
    public Long score;

    public LeaderBoardDto(String userName, Long participant_id, Long score) {
        this.participant_id = participant_id;
        this.userName = userName;
        this.score = score;
    }
    @Override
    public String toString() {
    return "LeaderBoardDto{" +
            "participant_id=" + participant_id +
            ", userName='" + userName + '\'' +
            ", score=" + score +
            '}';
}
}

// public interface LeaderBoardView {
//     Long getParticipant_id();
//     String getUserName();
//     Long getScore();
// }