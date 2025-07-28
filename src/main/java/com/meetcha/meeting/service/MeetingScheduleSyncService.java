package com.meetcha.meeting.service;

import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.external.google.GoogleCalendarClient;
import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.joinmeeting.domain.MeetingParticipant;
import com.meetcha.joinmeeting.domain.MeetingParticipantRepository;
import com.meetcha.meeting.domain.MeetingEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingScheduleSyncService {
//확정된 미팅을 각 참가자의 Google Calendar에 자동 등록
    private final MeetingParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final GoogleCalendarClient googleCalendarClient;

    @Transactional(readOnly = true)
    public void syncMeetingToCalendars(MeetingEntity meeting) {
        // 1. 미팅 참여자 조회
        List<MeetingParticipant> participants = participantRepository.findAllByMeetingId(meeting.getMeetingId());

        for (MeetingParticipant participant : participants) {
            // 2. 사용자 조회
            UserEntity user = userRepository.findById(participant.getUserId())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            // 3. Google Access Token 확인
            String accessToken = user.getGoogleToken();
            if (accessToken == null || accessToken.isBlank()) {
                continue; // 토큰이 없는 사용자는 건너뜀
            }

            // 4. Google Calendar 일정 등록
            googleCalendarClient.createEvent(
                    accessToken,
                    meeting.getTitle(),
                    meeting.getConfirmedTime(),
                    meeting.getConfirmedTime().plusMinutes(meeting.getDurationMinutes()),
                    null // 반복 없음
            );
        }
    }
}
