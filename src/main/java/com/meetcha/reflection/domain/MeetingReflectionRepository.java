package com.meetcha.reflection.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MeetingReflectionRepository extends JpaRepository<MeetingReflectionEntity, UUID> {
    //사용자가 이 미팅에 대해 이미 회고를 작성했는지 확인
    Optional<MeetingReflectionEntity> findByMeeting_MeetingIdAndUser_UserId(UUID meetingId, UUID userId);
}
