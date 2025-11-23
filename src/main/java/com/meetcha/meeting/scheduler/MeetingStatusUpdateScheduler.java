package com.meetcha.meeting.scheduler;

import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;
import com.meetcha.meeting.domain.MeetingStatus;
import com.meetcha.meeting.service.MeetingConfirmationService;
import com.meetcha.meeting.service.MeetingScheduleSyncService;
import com.meetcha.meetinglist.domain.AlternativeTimeEntity;
import com.meetcha.meetinglist.repository.AlternativeTimeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MeetingStatusUpdateScheduler {
//매 5분마다 미팅의 현재 상태를 시간 흐름에 따라 자동 전이하는 역할
//사용자 입력 없이, 시스템이 시간 흐름을 따라 미팅 상태를 관리한다.
    private final MeetingRepository meetingRepository;
    private final AlternativeTimeRepository alternativeTimeRepository;
    private final MeetingScheduleSyncService syncService;
    private final MeetingConfirmationService confirmationService;

    @Scheduled(fixedRate = 60 * 1000) // 매 1분마다 실행
    public void updateMeetingStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // 1. BEFORE → ONGOING
        List<MeetingEntity> toStart = meetingRepository.findByMeetingStatusAndConfirmedTimeBefore(
                MeetingStatus.BEFORE, now
        );
        toStart.forEach(meeting -> {
            meeting.setMeetingStatus(MeetingStatus.ONGOING);
            log.info("[미팅 시작] {} → ONGOING", meeting.getMeetingId());
        });

        // 2. ONGOING → DONE
        List<MeetingEntity> toEnd = meetingRepository.findAll().stream()
                .filter(m -> m.getMeetingStatus() == MeetingStatus.ONGOING)
                .filter(m -> m.getConfirmedTime().plusMinutes(m.getDurationMinutes()).isBefore(now))
                .toList();

        toEnd.forEach(meeting -> {
            meeting.setMeetingStatus(MeetingStatus.DONE);
            log.info("[미팅 종료] {} → DONE", meeting.getMeetingId());
        });

        meetingRepository.saveAll(toStart);
        meetingRepository.saveAll(toEnd);
    }

    /**
     * 참여 마감시간이 지난 MATCHING 미팅들에 대해
     * 1단계 확정 로직(confirmMeeting)을 자동으로 수행
     */
    @Scheduled(fixedRate = 60_000) // 1분마다
    @Transactional
    public void confirmMeetingForDeadlinePassed() {
        LocalDateTime now = LocalDateTime.now();
        log.info("[Scheduler] confirmMeetingForDeadlinePassed now={}", now);

        List<MeetingEntity> targets =
                meetingRepository.findByMeetingStatusAndConfirmedTimeIsNullAndDeadlineBefore(
                        MeetingStatus.MATCHING, now
                );

        log.info("[Scheduler] 참여 마감 지난 MATCHING 미팅 수 = {}", targets.size());

        for (MeetingEntity meeting : targets) {
            try {
                log.info("[Scheduler] confirmMeeting 호출: meetingId={}", meeting.getMeetingId());
                confirmationService.confirmMeeting(meeting.getMeetingId());
            } catch (Exception e) {
                log.error("[Scheduler] confirmMeeting 실패: meetingId={}", meeting.getMeetingId(), e);
            }
        }
    }


    @Scheduled(fixedRate = 60 * 1000) // 매 1분마다 실행
    @Transactional
    public void confirmFromAlternativeTimes() {
        LocalDateTime now = LocalDateTime.now();
        List<MeetingEntity> targets = meetingRepository.findMeetingsToConfirmFromAlternativeForUpdate(now);

        for (MeetingEntity meeting : targets) {
            Optional<AlternativeTimeEntity> confirmed = alternativeTimeRepository
                    .findTopByMeetingIdOrderByVoteCountDescStartTimeAsc(meeting.getMeetingId());

            /// 투표 결과가 있으면 그 시간(confirmed.get().getStartTime())을 미팅 확정 시간으로 저장
            if (confirmed.isPresent()) {
                meeting.setConfirmedTime(confirmed.get().getStartTime());
                meeting.setMeetingStatus(MeetingStatus.BEFORE);
                meetingRepository.save(meeting);

                syncService.syncMeetingToCalendars(meeting); // 구글 캘린더 연동
            } else {
                /// 아무도 투표 안하고, 시간이 끝난 경우 실패
                meeting.setMeetingStatus(MeetingStatus.MATCH_FAILED);
                meetingRepository.save(meeting);
            }
        }
    }
}
