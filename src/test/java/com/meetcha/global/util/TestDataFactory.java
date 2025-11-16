package com.meetcha.global.util;

import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.meeting.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class TestDataFactory {
    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private UserRepository userRepository;

    public UserEntity createUser(String email) {
        LocalDateTime now = LocalDateTime.now();
        UserEntity user = new UserEntity(null, "사용자1", email, "token", now, null, "refresh", now.plusDays(3));
        return userRepository.save(user);
    }

    public MeetingEntity createMeeting(UUID userId) {
        LocalDateTime now = LocalDateTime.now();

        List<LocalDate> candDates = List.of(LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth() + 5), LocalDate.of(now.getYear(), now.getMonth(), now.getDayOfMonth() + 6));

        MeetingEntity meeting = MeetingEntity.builder()
                .title("미팅1")
                .description("미팅1입니다")
                .durationMinutes(60)
                .deadline(now.plusDays(2))
                .createdAt(now)
                .meetingStatus(MeetingStatus.MATCHING)
                .confirmedTime(null)
                .createdBy(userId)
                .meetingCode(UUID.randomUUID().toString().substring(0, 8))
                .build();

        List<MeetingCandidateDateEntity> candidateDates = candDates.stream()
                .map(date -> new MeetingCandidateDateEntity(meeting, date))
                .toList();

        meeting.setCandidateDates(candidateDates);

        return meetingRepository.save(meeting);
    }
}
