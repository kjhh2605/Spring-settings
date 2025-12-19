package com.myApp.domain.users.service;

import com.myApp.domain.users.dto.UserResponseDto;
import com.myApp.auth.entity.Member;
import com.myApp.auth.repository.MemberRepository;
import com.myApp.global.apiPayload.code.status.GeneralErrorCode;
import com.myApp.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final MemberRepository memberRepository;

    public UserResponseDto getUser(Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.USER_NOT_FOUND));
        return UserResponseDto.from(member);
    }

    public UserResponseDto getMyInfo(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new GeneralException(GeneralErrorCode.USER_NOT_FOUND));
        return UserResponseDto.from(member);
    }
}
