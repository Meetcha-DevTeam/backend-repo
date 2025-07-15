package com.meetcha.joinmeeting.service;

import com.meetcha.joinmeeting.domain.MeetingParticipant;
import com.meetcha.joinmeeting.domain.MeetingParticipantRepository;
import com.meetcha.joinmeeting.domain.ParticipantAvailability;
import com.meetcha.joinmeeting.domain.ParticipantAvailabilityRepository;
import com.meetcha.joinmeeting.dto.JoinMeetingRequest;
import com.meetcha.joinmeeting.dto.JoinMeetingResponse;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JoinMeetingService {

    private final MeetingParticipantRepository participantRepository;
    private final ParticipantAvailabilityRepository availabilityRepository;
    private final MeetingRepository meetingRepository;

    @Transactional
    public JoinMeetingResponse join(UUID meetingId, JoinMeetingRequest request) {
        // todo 아직 SecurityContextHolder에 사용자정보 저장이 안되어있음 추후 추가하기
        UUID userId = getCurrentUserId();

        // 중복 참가 방지
        if (participantRepository.existsByMeetingIdAndUserId(meetingId, userId)) {
            throw new RuntimeException("중복 참가");
        }

        // 참가자 저장
        MeetingParticipant participant = MeetingParticipant.create(
                userId,
                meetingId,
                request.nickname()
        );
        participantRepository.save(participant);

        //  선택 시간 저장
        List<ParticipantAvailability> availabilities = request.selectedTimes().stream()
                .map(slot -> ParticipantAvailability.create(
                        participant.getParticipantId(),
                        meetingId,
                        slot.startAt(),
                        slot.endAt()
                ))
                .toList();

        availabilityRepository.saveAll(availabilities);

        // 응답 반환
        return new JoinMeetingResponse(meetingId, participant.getParticipantId());
    }

    public void validateMeetingCode(String code) {
        MeetingEntity meeting = meetingRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("미팅을 찾을 수 없습니다"));//todo

        if (meeting.getDeadline().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("미팅 참여마감시간이 지났습니다.");//todo
        }
    }



    protected UUID getCurrentUserId() {
        // TODO: SecurityContextHolder구현 이후 실제 userId 추출
        return UUID.randomUUID(); // 예시용
    }


}

