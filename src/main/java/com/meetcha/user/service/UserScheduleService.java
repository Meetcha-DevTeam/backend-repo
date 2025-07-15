package com.meetcha.user.service;

import com.meetcha.user.domain.UnavailableTime;
import com.meetcha.user.dto.BusyTimeResponse;
import com.meetcha.user.domain.UnavailableTimeRepository;
import com.meetcha.user.util.UnavailableTimeConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserScheduleService {

    private final UnavailableTimeRepository unavailableTimeRepository;

    //유저 바쁜 스케줄 조회
    public List<BusyTimeResponse> getBusyTimes(UUID userId, LocalDateTime from, LocalDateTime to) {
        List<UnavailableTime> unavailableRules = unavailableTimeRepository.findAllByUserId(userId);
        //LocalTime-> LocalDateTime 변환 후 반환
        return UnavailableTimeConverter.convertToBusyTimes(unavailableRules, from, to);
    }

}
