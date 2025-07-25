package com.meetcha.meetinglist.service;

import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.exception.InvalidJoinMeetingRequestException;
import com.meetcha.meeting.domain.MeetingEntity;
import com.meetcha.meeting.domain.MeetingRepository;

import com.meetcha.meetinglist.domain.ParticipantEntity;
import com.meetcha.meetinglist.dto.MeetingDetailResponse;
import com.meetcha.meetinglist.dto.ParticipantDto;
import com.meetcha.meetinglist.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeetingListService {

    private final MeetingRepository meetingRepository;
    private final ParticipantRepository participantRepository;

    public MeetingDetailResponse getMeetingDetail(UUID meetingId, String authorizationHeader) {
        // 미팅 상세 조회 로직
        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new InvalidJoinMeetingRequestException(ErrorCode.MEETING_NOT_FOUND));


        // 참여자 조회
        List<ParticipantEntity> participantEntities = participantRepository.findByMeeting_MeetingId(meetingId);

        List<ParticipantDto> participantDtos = participantEntities.stream()
                .map(p -> new ParticipantDto(
                        p.getId(),
                        p.getNickname(),
                        p.getProfileImageUrl()
                ))
                .toList();



        return new MeetingDetailResponse(
                meeting.getMeetingId(),
                meeting.getTitle(),
                meeting.getDescription(),
                meeting.getMeetingStatus(),
                meeting.getDeadline(),
                meeting.getDurationMinutes(),
                meeting.getConfirmedTime(),
                participantDtos
        );
    }}

/*    public ParticipantsResponse getParticipants(UUID meetingId, String authorizationHeader) {
        //미팅 참가자 목록 조회 로직 (이거 안해도 될수도)
        return null;
    }
//}*/
