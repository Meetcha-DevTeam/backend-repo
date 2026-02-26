package com.meetcha;

import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class TestDataFactory {

    /**
     테스트용 Meeting 생성 메서드
     **/

    public static MeetingEntity createDoneMeeting(
            UUID createdBy,
            String title,
            int durationMinutes
    ) {
        //기준 시간
        LocalDateTime now = LocalDateTime.now();

        //테스트 기본값
        LocalDateTime deadline = now.plusDays(1);
        String meetingCode = "MEET-" + UUID.randomUUID();

        return MeetingEntity.builder()
                //입력 파라미터
                .title(title)
                .durationMinutes(durationMinutes)
                .createdBy(createdBy)

                //테스트용 고정 값
                .description("테스트용 미팅 설명")
                .meetingStatus(MeetingStatus.DONE)
                .meetingCode(meetingCode)

                //시간 관련
                .createdAt(now)
                .confirmedTime(now)
                .deadline(deadline)

                .build();
    }

     /**
     회고 생성 요청용 Request Body
     **/
    public static Map<String, Object> createReflectionRequest(
            int contribution,
            String role,
            String thought,
            String completedWork,
            String plannedWork
    ) {
        return Map.of(
                "contribution", contribution,
                "role", role,
                "thought", thought,
                "completedWork", completedWork,
                "plannedWork", plannedWork
        );
    }
}
