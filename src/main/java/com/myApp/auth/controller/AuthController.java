package com.myApp.auth.controller;

import com.myApp.auth.dto.TokenDto;
import com.myApp.auth.service.AuthService;
import com.myApp.global.apiPayload.ApiResponse;
import com.myApp.global.apiPayload.code.status.GeneralSuccessCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;

    @Value("${spring.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenValidityInSeconds;

    @PostMapping("/reissue")
    public ApiResponse<TokenDto> reissue(@CookieValue("refresh_token") String refreshToken,
            HttpServletResponse response) {
        TokenDto tokenDto = authService.reissue(refreshToken);

        // Refresh Token을 HttpOnly Cookie로 설정
        ResponseCookie cookie = ResponseCookie.from("refresh_token", tokenDto.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenValidityInSeconds)
                .sameSite("None")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        // Body에는 Access Token만 내려주기 위해 Refresh Token을 null로 설정 (또는 별도 DTO 사용)
        // 여기서는 편의상 TokenDto를 그대로 쓰되 refreshToken 필드를 null로 설정하거나 프론트에서 무시하도록 함.
        // 더 깔끔하게 하려면 AccessTokenDto를 별도로 만드는 것이 좋음.
        // 일단은 TokenDto의 refreshToken을 null로 설정해서 보냄.
        TokenDto responseDto = TokenDto.builder()
                .grantType(tokenDto.getGrantType())
                .accessToken(tokenDto.getAccessToken())
                .accessTokenExpiresIn(tokenDto.getAccessTokenExpiresIn())
                .refreshToken(null) // Body에서 제거
                .build();

        return ApiResponse.onSuccess(GeneralSuccessCode._OK, responseDto);
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader("Authorization") String accessToken,
            @CookieValue("refresh_token") String refreshToken,
            HttpServletResponse response) {
        // Bearer 제거
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        authService.logout(accessToken, refreshToken);

        // 쿠키 삭제
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // 만료
                .sameSite("None")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ApiResponse.onSuccess(GeneralSuccessCode._OK, "로그아웃 되었습니다.");
    }
}
