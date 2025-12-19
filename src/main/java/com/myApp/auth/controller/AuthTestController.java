package com.myApp.auth.controller;

import com.myApp.auth.dto.TokenDto;
import com.myApp.auth.jwt.JwtTokenProvider;
import com.myApp.auth.redis.RefreshToken;
import com.myApp.auth.repository.RefreshTokenRepository;
import com.myApp.auth.entity.User;
import com.myApp.auth.repository.UserRepository;
import com.myApp.auth.entity.Role;
import com.myApp.global.apiPayload.ApiResponse;
import com.myApp.global.apiPayload.code.status.GeneralSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.Token;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/v1/auth/test")
@RequiredArgsConstructor
@Profile("dev")
public class AuthTestController {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Operation(summary = "Dev용 로그인 (토큰 발급)", description = "개발 환경에서 OAuth2 로그인 없이 토큰을 발급받습니다.")
    @GetMapping("/login")
    public ApiResponse<TokenDto> devLogin(@RequestParam String email, HttpServletResponse response) {
        // 1. 사용자 확인 및 강제 생성 (테스트 편의성)
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .name("Dev User")
                        .role(Role.USER)
                        .socialType("DEV")
                        .socialId("dev_" + email)
                        .build()));

        // 2. Authentication 객체 생성
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                Collections.singleton(new SimpleGrantedAuthority(user.getRoleKey())));

        // 3. 토큰 생성
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(authentication);

        // 4. Refresh Token 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .id(user.getEmail())
                .token(tokenDto.getRefreshToken())
                .build();
        refreshTokenRepository.save(refreshToken);

        // 5. 쿠키 설정
        ResponseCookie cookie = ResponseCookie.from("refresh_token", tokenDto.getRefreshToken())
                .httpOnly(true)
                .secure(false) // Dev 환경이므로 false
                .path("/")
                .maxAge(60 * 60 * 24 * 7) // 7일
                .sameSite("None")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ApiResponse.onSuccess(GeneralSuccessCode._OK, tokenDto);
    }
}
