package com.myApp.auth.service;

import com.myApp.auth.dto.TokenDto;
import com.myApp.auth.jwt.JwtTokenProvider;
import com.myApp.auth.redis.RefreshToken;
import com.myApp.auth.repository.RefreshTokenRepository;
import com.myApp.global.apiPayload.code.status.AuthErrorCode;
import com.myApp.global.apiPayload.code.status.GeneralErrorCode;
import com.myApp.global.apiPayload.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenDto reissue(String refreshToken) {
        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new GeneralException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 2. Access Token 에서 User ID 가져오기
        // (Refresh Token 에는 권한 정보가 없을 수 있으므로, Access Token 이 만료되었더라도 파싱해서 ID를 가져오거나,
        // Refresh Token 에 ID를 넣어서 파싱)
        // 여기서는 Refresh Token 을 파싱해서 Authentication 을 가져오는 방식을 사용 (JwtTokenProvider 구현에
        // 따라 다름)
        // JwtTokenProvider.getAuthentication 은 Access Token 용으로 설계되었으므로, Refresh Token
        // 용으로 별도 메서드가 필요할 수 있음.
        // 하지만 여기서는 Refresh Token 도 JWT 이므로, claims 에서 sub 를 가져올 수 있음.
        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);

        // 3. Redis 에서 User ID 를 기반으로 저장된 Refresh Token 값을 가져옴
        RefreshToken redisRefreshToken = refreshTokenRepository.findById(authentication.getName())
                .orElseThrow(() -> new GeneralException(AuthErrorCode.INVALID_REFRESH_TOKEN));

        // 4. Refresh Token 일치하는지 검사
        if (!redisRefreshToken.getToken().equals(refreshToken)) {
            throw new GeneralException(AuthErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        // 5. 새로운 토큰 생성
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(authentication);

        // 6. Refresh Token 업데이트
        redisRefreshToken.updateToken(tokenDto.getRefreshToken());
        refreshTokenRepository.save(redisRefreshToken);

        return tokenDto;
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        // 1. Access Token 검증
        if (!jwtTokenProvider.validateToken(accessToken)) {
            throw new GeneralException(AuthErrorCode.AUTH_TOKEN_INVALID);
        }

        // 2. Access Token 에서 User ID 가져오기
        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);

        // 3. Redis 에서 해당 User ID 로 저장된 Refresh Token 이 있는지 여부를 확인 후 있을 경우 삭제
        if (refreshTokenRepository.findById(authentication.getName()).isPresent()) {
            refreshTokenRepository.deleteById(authentication.getName());
        }
    }
}
