package com.meetcha.meetinglist.service;

import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.exception.InvalidAlternativeTimeException;
import com.meetcha.meetinglist.domain.AlternativeTimeEntity;
import com.meetcha.meetinglist.domain.AlternativeVoteEntity;
import com.meetcha.meetinglist.dto.AlternativeTimeDto;
import com.meetcha.meetinglist.dto.AlternativeTimeListResponse;
import com.meetcha.meetinglist.dto.AlternativeVoteRequest;
import com.meetcha.meetinglist.dto.AlternativeVoteResponse;
import com.meetcha.meetinglist.repository.AlternativeTimeRepository;
import com.meetcha.meetinglist.repository.AlternativeVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlternativeTimeService {
    private final AlternativeTimeRepository alternativeTimeRepository;
    private final AlternativeVoteRepository alternativeVoteRepository;

    public AlternativeTimeListResponse getAlternativeTimeList(UUID meetingId, String authorizationHeader) {
        //대안시간 후보 조회 로직
        // 1. 사용자 식별
        // todo 아직 SecurityContextHolder에 사용자정보 저장이 안되어있음 추후 추가하기
        UUID userId = getCurrentUserId();

        // 2. 후보 시간 조회
        List<AlternativeTimeEntity> entities = alternativeTimeRepository.findByMeetingId(meetingId);

        // 3. DTO 변환 (+ 현재 사용자 체크 여부, 총 투표 수 포함)
        List<AlternativeTimeDto> dtoList = entities.stream()
                .map(entity -> {
                    int voteCnt = alternativeVoteRepository.countByAlternativeTimeIdAndCheckedTrue(entity.getAlternativeTimeId());
                    boolean checked = alternativeVoteRepository.existsByAlternativeTimeIdAndUserIdAndCheckedTrue(entity.getAlternativeTimeId(), userId);
                    return AlternativeTimeDto.from(entity, voteCnt, checked);
                })
                .toList();

        return AlternativeTimeListResponse.of(dtoList);


    }


    @Transactional
    public AlternativeVoteResponse submitAlternativeVote(UUID meetingId, AlternativeVoteRequest request, String authorizationHeader) {
        //대안 시간 투표 제출 로직
        // todo 아직 SecurityContextHolder에 사용자정보 저장이 안되어있음 추후 추가하기
        UUID userId = getCurrentUserId();

        // 1. 해당 시간에 해당하는 후보 조회
        AlternativeTimeEntity timeEntity = alternativeTimeRepository
                .findByMeetingIdAndStartTime(meetingId, request.getAlternativeTime().toLocalDateTime())
                .orElseThrow(() -> new InvalidAlternativeTimeException(ErrorCode.MEETING_NOT_FOUND));///CustomException 로 통합

        // 2. 이미 투표했는지 확인
        boolean alreadyVoted = alternativeVoteRepository.existsByAlternativeTime_MeetingIdAndUserIdAndCheckedTrue(meetingId, userId);
        if (alreadyVoted) {
            throw new CustomException(ErrorCode.ALREADY_VOTED_ALTERNATIVE);///
        }

        // 3. 대안시간 투표 저장
        AlternativeVoteEntity vote = AlternativeVoteEntity.builder()
                .alternativeTimeId(timeEntity.getAlternativeTimeId())
                .userId(userId)
                .checked(true)
                .build();

        vote = alternativeVoteRepository.save(vote);

        return AlternativeVoteResponse.builder()
                .voteId(vote.getVoteId())
                .build();
    }

    protected UUID getCurrentUserId() {
        // TODO: SecurityContextHolder구현 이후 실제 userId 추출
        return UUID.randomUUID(); // 예시용
    }
}
