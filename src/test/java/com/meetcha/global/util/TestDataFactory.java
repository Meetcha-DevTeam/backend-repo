package com.meetcha.global.util;

import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;
import com.meetcha.meeting.domain.MeetingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
        return meetingRepository.save(meeting);
    }
}
