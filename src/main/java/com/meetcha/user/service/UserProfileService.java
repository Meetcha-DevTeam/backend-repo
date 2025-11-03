package com.meetcha.user.service;

import com.meetcha.auth.domain.UserEntity;
import com.meetcha.auth.domain.UserRepository;
import com.meetcha.global.exception.CustomException;
import com.meetcha.user.dto.MyPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.meetcha.user.exception.UserErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserRepository userRepository;

    public MyPageResponse getMyPage(UUID userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(()->new CustomException(USER_NOT_FOUND));
        return new MyPageResponse(
                user.getName(),
                user.getProfileImgUrl()
        );
    }
}
