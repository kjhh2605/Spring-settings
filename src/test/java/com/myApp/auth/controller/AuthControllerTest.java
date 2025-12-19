package com.myApp.auth.controller;

import com.myApp.auth.dto.TokenDto;
import com.myApp.auth.service.AuthService;
import com.myApp.global.apiPayload.code.status.GeneralSuccessCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseCookie;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("토큰 재발급 테스트")
    @WithMockUser
    void reissue() throws Exception {
        // given
        TokenDto tokenDto = TokenDto.builder()
                .accessToken("newAccessToken")
                .refreshToken("newRefreshToken")
                .build();

        ResponseCookie cookie = ResponseCookie.from("refresh_token", "newRefreshToken")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .build();

        given(authService.reissue(anyString())).willReturn(tokenDto);
        given(authService.createRefreshTokenCookie(anyString())).willReturn(cookie);

        // when & then
        mockMvc.perform(post("/api/v1/auth/reissue")
                .with(csrf())
                .cookie(new jakarta.servlet.http.Cookie("refresh_token", "refreshToken")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.result").value("newAccessToken"))
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("로그아웃 테스트")
    @WithMockUser
    void logout() throws Exception {
        // given
        // when & then
        mockMvc.perform(post("/api/v1/auth/logout")
                .with(csrf())
                .header("Authorization", "Bearer accessToken")
                .cookie(new jakarta.servlet.http.Cookie("refresh_token", "refreshToken")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("로그아웃 되었습니다."));
    }
}
