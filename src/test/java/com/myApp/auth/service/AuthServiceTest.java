package com.myApp.auth.service;

import com.myApp.auth.dto.TokenDto;
import com.myApp.auth.jwt.JwtTokenProvider;
import com.myApp.auth.token.TokenStore;
import com.myApp.global.apiPayload.exception.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TokenStore tokenStore;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("토큰 재발급 성공")
    void reissue_Success() {
        // given
        String refreshTokenStr = "validRefreshToken";
        TokenDto newTokenDto = TokenDto.builder()
                .accessToken("newAccess")
                .refreshToken("newRefresh")
                .build();

        given(jwtTokenProvider.validateToken(refreshTokenStr)).willReturn(true);
        given(jwtTokenProvider.getSubject(refreshTokenStr)).willReturn("user1");
        given(tokenStore.findRefreshToken("user1")).willReturn(Optional.of(refreshTokenStr));
        given(customUserDetailsService.loadUserByUsername("user1")).willReturn(new User(
                "user1",
                "",
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
        ));
        given(jwtTokenProvider.generateTokenDto(any(Authentication.class))).willReturn(newTokenDto);

        // when
        TokenDto result = authService.reissue(refreshTokenStr);

        // then
        assertThat(result.getAccessToken()).isEqualTo("newAccess");
        verify(tokenStore).saveRefreshToken("user1", "newRefresh");
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 리프레시 토큰 불일치")
    void reissue_Mismatch() {
        // given
        String refreshTokenStr = "validRefreshToken";

        given(jwtTokenProvider.validateToken(refreshTokenStr)).willReturn(true);
        given(jwtTokenProvider.getSubject(refreshTokenStr)).willReturn("user1");
        given(tokenStore.findRefreshToken("user1")).willReturn(Optional.of("differentToken"));

        // when & then
        assertThatThrownBy(() -> authService.reissue(refreshTokenStr))
                .isInstanceOf(GeneralException.class);
    }
}
