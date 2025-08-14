package com.meetcha.meeting.domain;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "meeting_candidate_dates")
@Getter
@NoArgsConstructor
public class MeetingCandidateDateEntity {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private UUID id;

    @Column(name = "candidate_date", nullable = false)
    private LocalDate candidateDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private MeetingEntity meeting;

    //생성자
    public MeetingCandidateDateEntity(MeetingEntity meeting, LocalDate candidateDate) {
        this.meeting = meeting;
        this.candidateDate = candidateDate;
    }
}