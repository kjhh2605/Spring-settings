package com.myApp.auth.service;

import com.myApp.auth.dto.TokenDto;
import com.myApp.auth.jwt.JwtTokenProvider;
import com.myApp.auth.redis.RefreshToken;
import com.myApp.auth.repository.RefreshTokenRepository;
import com.myApp.global.apiPayload.exception.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Test
    @DisplayName("토큰 재발급 성공")
    void reissue_Success() {
        // given
        String refreshTokenStr = "validRefreshToken";
        Authentication authentication = mock(Authentication.class);
        given(authentication.getName()).willReturn("user1");

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .id("user1")
                .token(refreshTokenStr)
                .build();

        TokenDto newTokenDto = TokenDto.builder()
                .accessToken("newAccess")
                .refreshToken("newRefresh")
                .build();

        given(jwtTokenProvider.validateToken(refreshTokenStr)).willReturn(true);
        given(jwtTokenProvider.getAuthentication(refreshTokenStr)).willReturn(authentication);
        given(refreshTokenRepository.findById("user1")).willReturn(Optional.of(refreshTokenEntity));
        given(jwtTokenProvider.generateTokenDto(authentication)).willReturn(newTokenDto);

        // when
        TokenDto result = authService.reissue(refreshTokenStr);

        // then
        assertThat(result.getAccessToken()).isEqualTo("newAccess");
        assertThat(refreshTokenEntity.getToken()).isEqualTo("newRefresh");
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 리프레시 토큰 불일치")
    void reissue_Mismatch() {
        // given
        String refreshTokenStr = "validRefreshToken";
        Authentication authentication = mock(Authentication.class);
        given(authentication.getName()).willReturn("user1");

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .id("user1")
                .token("differentToken")
                .build();

        given(jwtTokenProvider.validateToken(refreshTokenStr)).willReturn(true);
        given(jwtTokenProvider.getAuthentication(refreshTokenStr)).willReturn(authentication);
        given(refreshTokenRepository.findById("user1")).willReturn(Optional.of(refreshTokenEntity));

        // when & then
        assertThatThrownBy(() -> authService.reissue(refreshTokenStr))
                .isInstanceOf(GeneralException.class);
    }
}
