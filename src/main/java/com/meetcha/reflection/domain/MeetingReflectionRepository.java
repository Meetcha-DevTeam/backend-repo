package com.meetcha.reflection.domain;

import com.meetcha.reflection.dto.GetReflectionResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MeetingReflectionRepository extends JpaRepository<MeetingReflectionEntity, UUID> {
    //사용자가 이 미팅에 대해 이미 회고를 작성했는지 확인
    Optional<MeetingReflectionEntity> findByMeeting_MeetingIdAndUser_UserId(UUID meetingId, UUID userId);

    //해당 미팅에 대한 회고가 존재하는지 확인
    boolean existsByMeeting_MeetingIdAndUser_UserId(UUID meetingId, UUID userId);

    //Optional<GetReflectionResponse> findReflectionDetail(UUID meetingId, UUID userId);
}
