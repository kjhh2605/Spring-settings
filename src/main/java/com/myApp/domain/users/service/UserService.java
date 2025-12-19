package com.myApp.domain.users.service;

import com.myApp.domain.users.dto.UserResponseDto;
import com.myApp.auth.entity.User;
import com.myApp.auth.repository.UserRepository;
import com.myApp.global.apiPayload.code.status.GeneralErrorCode;
import com.myApp.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserResponseDto getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.USER_NOT_FOUND));
        return UserResponseDto.from(user);
    }

    public UserResponseDto getMyInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.USER_NOT_FOUND));
        return UserResponseDto.from(user);
    }
}
