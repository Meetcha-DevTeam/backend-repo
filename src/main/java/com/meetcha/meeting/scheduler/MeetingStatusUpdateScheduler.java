package com.meetcha.meeting.scheduler;

import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;
import com.meetcha.meeting.domain.MeetingStatus;
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

    @Scheduled(fixedRate = 60 * 1000) // 매 1분마다 실행
    @Transactional
    public void confirmFromAlternativeTimes() {
//        List<MeetingEntity> targets = meetingRepository.findMeetingsToConfirmFromAlternative();
        List<MeetingEntity> targets = meetingRepository.findMeetingsToConfirmFromAlternativeForUpdate();

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
