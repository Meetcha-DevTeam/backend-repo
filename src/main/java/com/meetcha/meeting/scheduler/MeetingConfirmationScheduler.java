package com.meetcha.meeting.scheduler;

import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;
import com.meetcha.meeting.domain.MeetingStatus;
import com.meetcha.meeting.service.MeetingConfirmationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MeetingConfirmationScheduler {

    private final MeetingRepository meetingRepository;
    private final MeetingConfirmationService meetingConfirmationService;

    //매 10분마다 참여 마감된 미팅을 확인하고 자동 확정 - 1분마다 하는건 어때

    @Scheduled(fixedRate = 10 * 60 * 1000) // 10분 간격
    public void autoConfirmMeetings() {
        LocalDateTime now = LocalDateTime.now();

        // 참여 마감 시간이 지났고 아직 확정되지 않은 미팅 찾기
        List<MeetingEntity> expired = meetingRepository
                .findByMeetingStatusAndDeadlineBefore(MeetingStatus.MATCHING, now);

        for (MeetingEntity meeting : expired) {
            try {
                log.info("[자동확정] 미팅 ID: {}", meeting.getMeetingId());
                meetingConfirmationService.confirmMeeting(meeting.getMeetingId());
            } catch (Exception e) {
                log.error("미팅 자동 확정 실패 - ID: {}", meeting.getMeetingId(), e);
            }
        }
    }
}
