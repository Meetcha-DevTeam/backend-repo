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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeetingScheduleSyncService {

    private final MeetingParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final GoogleCalendarClient googleCalendarClient;

    /**
     * 미팅 확정 후 모든 참여자의 Google Calendar에 일정 등록
     */
    @Transactional(readOnly = true)
    public void syncMeetingToCalendars(MeetingEntity meeting) {
        List<MeetingParticipant> participants = participantRepository.findAllByMeetingId(meeting.getMeetingId());

        for (MeetingParticipant participant : participants) {
            // 1. 사용자 조회
            UserEntity user = userRepository.findById(participant.getUserId())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            String accessToken = user.getGoogleToken();
            if (accessToken == null || accessToken.isBlank()) {
                log.warn("Google access token 없음 → 사용자 제외: {}", user.getEmail());
                continue;
            }

            // 2. 일정 생성
            try {
                googleCalendarClient.createEvent(
                        accessToken,
                        "[미팅] " + meeting.getTitle(),
                        meeting.getConfirmedTime(),
                        meeting.getConfirmedTime().plusMinutes(meeting.getDurationMinutes()),
                        null // 반복 없음
                );
                log.info("Google Calendar 등록 완료: {}", user.getEmail());
            } catch (Exception e) {
                log.error("Google 일정 등록 실패 - 사용자: {}", user.getEmail(), e);
            }
        }
    }
}
