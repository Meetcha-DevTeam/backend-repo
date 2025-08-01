package com.meetcha.meeting.service.algorithm;

//POJO
// aws lambda 확장을 위해 라이브러리  최소 사용(lombok X)
import java.util.List;

public class Meeting {

    private final String id;
    private final List<Participant> participants;
    private final int deadline; // 사용하지 않으면 0으로
    private final int duration; // 분 단위
    private final Integer meetingTime; // 산출된 미팅 시간 (분 단위)
    private final Integer alternativeTime;
    private final List<Integer> alternativeCandidates;
    private final int alternativeDeadline;
    private final List<Integer> candidateDay;

    public Meeting(
            String id,
            List<Participant> participants,
            int deadline,
            int duration,
            Integer meetingTime,
            Integer alternativeTime,
            List<Integer> alternativeCandidates,
            int alternativeDeadline,
            List<Integer> candidateDay
    ) {
        this.id = id;
        this.participants = participants;
        this.deadline = deadline;
        this.duration = duration;
        this.meetingTime = meetingTime;
        this.alternativeTime = alternativeTime;
        this.alternativeCandidates = alternativeCandidates;
        this.alternativeDeadline = alternativeDeadline;
        this.candidateDay = candidateDay;
    }

    // Getter 메서드만 필요 (Setter는 불필요)
    public String getId() {
        return id;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public int getDeadline() {
        return deadline;
    }

    public int getDuration() {
        return duration;
    }

    public Integer getMeetingTime() {
        return meetingTime;
    }

    public Integer getAlternativeTime() {
        return alternativeTime;
    }

    public List<Integer> getAlternativeCandidates() {
        return alternativeCandidates;
    }

    public int getAlternativeDeadline() {
        return alternativeDeadline;
    }

    public List<Integer> getCandidateDay() {
        return candidateDay;
    }
}
