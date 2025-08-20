package com.meetcha.meetinglist.service;

import com.meetcha.auth.jwt.JwtProvider;
import com.meetcha.global.exception.CustomException;
import com.meetcha.global.exception.ErrorCode;
import com.meetcha.global.exception.InvalidAlternativeTimeException;
import com.meetcha.global.util.AuthHeaderUtils;
import com.meetcha.global.util.DateTimeUtils;
import com.meetcha.joinmeeting.domain.MeetingParticipantRepository;
import com.meetcha.meetinglist.domain.AlternativeTimeEntity;
import com.meetcha.meetinglist.domain.AlternativeVoteEntity;
import com.meetcha.meetinglist.dto.AlternativeTimeDto;
import com.meetcha.meetinglist.dto.AlternativeTimeListResponse;
import com.meetcha.meetinglist.dto.AlternativeVoteRequest;
import com.meetcha.meetinglist.dto.AlternativeVoteResponse;
import com.meetcha.meetinglist.repository.AlternativeTimeRepository;
import com.meetcha.meetinglist.repository.AlternativeVoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlternativeTimeService {
    private final AlternativeTimeRepository alternativeTimeRepository;
    private final AlternativeVoteRepository alternativeVoteRepository;
    private final MeetingParticipantRepository meetingParticipantRepository;
    private final JwtProvider jwtProvider;

    public AlternativeTimeListResponse getAlternativeTimeList(UUID meetingId, String authorizationHeader) {
        //대안시간 후보 조회 로직
        // 1. 사용자 식별
        UUID userId = extractUserId(authorizationHeader);
        // 2. 후보 시간 조회
        List<AlternativeTimeEntity> entities = alternativeTimeRepository.findByMeetingId(meetingId);

        // 3. DTO 변환 (+ 현재 사용자 체크 여부, 총 투표 수 포함)
        List<AlternativeTimeDto> dtoList = entities.stream()
                .map(entity -> {
                    int voteCnt = alternativeVoteRepository.countByAlternativeTimeIdAndCheckedTrue(entity.getAlternativeTimeId());
                    boolean checked = alternativeVoteRepository.existsByAlternativeTimeIdAndUserIdAndCheckedTrue(entity.getAlternativeTimeId(), userId);
                    List<String> excludedNames = getExcludedNames(entity.getExcludedParticipants());
                    List<String> includedNames = getIncludedNames(meetingId, excludedNames);
                    return AlternativeTimeDto.from(entity, voteCnt, checked, excludedNames, includedNames);
                })
                .toList();

        return AlternativeTimeListResponse.of(dtoList);
    }

    private List<String> getExcludedNames(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private List<String> getIncludedNames(UUID meetingId, List<String> excludedNames) {
        // excludedNames 가 null이면 빈 리스트로 대체
        List<String> safeExcluded = Optional.ofNullable(excludedNames).orElse(List.of());

        // 전체 참여자 닉네임 조회 (null 방어)
        List<String> allParticipants =
                Optional.ofNullable(meetingParticipantRepository.findNicknamesByMeetingId(meetingId))
                        .orElse(List.of());

        return allParticipants.stream()
                .filter(name -> name != null && !safeExcluded.contains(name))
                .toList();
    }



    @Transactional
    public AlternativeVoteResponse submitAlternativeVote(UUID meetingId, AlternativeVoteRequest request, String authorizationHeader) {
        //대안 시간 투표 제출 로직
        UUID userId = extractUserId(authorizationHeader);
        // 1. 해당 시간에 해당하는 후보 조회
        AlternativeTimeEntity timeEntity = alternativeTimeRepository
                .findByMeetingIdAndStartTime(meetingId, DateTimeUtils.kstToUtc(request.getAlternativeTime()))
                .orElseThrow(() -> new InvalidAlternativeTimeException(ErrorCode.MEETING_NOT_FOUND));

        // 2. 이미 투표했는지 확인
        boolean alreadyVoted = alternativeVoteRepository.existsByAlternativeTime_MeetingIdAndUserIdAndCheckedTrue(meetingId, userId);
        if (alreadyVoted) {
            throw new CustomException(ErrorCode.ALREADY_VOTED_ALTERNATIVE);
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

    private UUID extractUserId(String authorizationHeader) {
        String token = AuthHeaderUtils.extractBearerToken(authorizationHeader);
        if (!jwtProvider.validateToken(token)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_USER);
        }
        return jwtProvider.getUserId(token);
    }
}
