package com.myApp.auth.controller;

import com.myApp.auth.dto.TokenDto;
import com.myApp.auth.service.AuthService;
import com.myApp.global.apiPayload.ApiResponse;
import com.myApp.global.apiPayload.code.status.GeneralSuccessCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

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

    @PostMapping("/reissue")
    public ApiResponse<String> reissue(@CookieValue("refresh_token") String refreshToken,
            HttpServletResponse response) {

        TokenDto tokenDto = authService.reissue(refreshToken);

        // Refresh Token Cookie 설정
        ResponseCookie cookie = authService.createRefreshTokenCookie(tokenDto.getRefreshToken());
        response.addHeader("Set-Cookie", cookie.toString());

        String accessToken = tokenDto.getAccessToken();
        response.setHeader("Authorization", "Bearer " + accessToken);

        return ApiResponse.onSuccess(GeneralSuccessCode._OK, accessToken);
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader("Authorization") String accessToken,
            @CookieValue("refresh_token") String refreshToken,
            HttpServletResponse response) {

        authService.logout(accessToken, refreshToken);

        // 쿠키 삭제 (빈 값으로 덮어쓰기)
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
