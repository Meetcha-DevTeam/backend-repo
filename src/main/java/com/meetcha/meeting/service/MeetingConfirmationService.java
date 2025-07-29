package com.meetcha.meeting.service;

import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.joinmeeting.domain.MeetingParticipantRepository;
import com.meetcha.joinmeeting.domain.ParticipantAvailability;
import com.meetcha.joinmeeting.domain.ParticipantAvailabilityRepository;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.meetcha.meeting.domain.MeetingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeetingConfirmationService {
//미팅 확정 로직
// 스케줄러뿐 아니라 사용자가 직접 확정할 때도 사용 가능.
    private final MeetingRepository meetingRepository;
    private final ParticipantAvailabilityRepository availabilityRepository;
    private final MeetingScheduleSyncService syncService;

    @Transactional
    public void confirmMeeting(UUID meetingId) {
        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEETING_NOT_FOUND));

        // 1. 참여자 가용 시간 조회
        List<ParticipantAvailability> allAvailability = availabilityRepository.findByMeetingId(meetingId);

        // 2. 알고리즘: 가장 겹치는 시간대 계산
        LocalDateTime bestSlot = extractBestTime(allAvailability, meeting.getDurationMinutes());

        // 3. 확정 시간 저장
        meeting.setConfirmedTime(bestSlot);
        meeting.setMeetingStatus(MeetingStatus.BEFORE);//todo 미팅확정시간은 항상 미팅 시작시간보다 앞서야한다.
        meetingRepository.save(meeting);

        // 4. 참여자들의 Google Calendar에 일정을 등록
        syncService.syncMeetingToCalendars(meeting);
    }

    private LocalDateTime extractBestTime(List<ParticipantAvailability> times, int durationMinutes) {
        // TODO: DP - 정렬 후 겹치는 구간 찾기 등 알고리즘 구현
        return times.get(0).getStartAt(); // 첫 번째 시간 사용
    }
}
