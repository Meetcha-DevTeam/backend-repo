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

     public static MeetingEntity createMeeting(
     UUID createdBy,
     String title,
     int durationMinutes
     ) {
     return MeetingEntity.builder()
     .title(title)
     .description("테스트용 미팅 설명")
     .durationMinutes(durationMinutes)
     .deadline(LocalDateTime.now().plusDays(1))
     .createdAt(LocalDateTime.now())
     .meetingStatus(MeetingStatus.DONE)
     .confirmedTime(LocalDateTime.now())

     .meetingCode("MEET-" + UUID.randomUUID()) // unique한 값이라 다른 테스트에서의 충돌을 막기위해 uuid를 추가했습니다
     .createdBy(createdBy)
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
