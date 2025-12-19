package com.myApp.auth.controller;

import com.myApp.domain.users.dto.UserResponseDto;
import com.myApp.auth.entity.Role;
import com.myApp.domain.users.controller.UserController;
import com.myApp.domain.users.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("내 정보 조회 성공")
    @WithMockUser(username = "test@example.com")
    void getMyInfo_Success() throws Exception {
        // given
        UserResponseDto responseDto = UserResponseDto.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .role(Role.USER)
                .socialType("google")
                .build();

        given(userService.getMyInfo("test@example.com")).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/users/me")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.result.email").value("test@example.com"));
    }

    @Test
    @DisplayName("유저 ID로 조회 성공")
    @WithMockUser
    void getUser_Success() throws Exception {
        // given
        Long userId = 1L;
        UserResponseDto responseDto = UserResponseDto.builder()
                .id(userId)
                .name("Target User")
                .email("target@example.com")
                .role(Role.USER)
                .socialType("kakao")
                .build();

        given(userService.getUser(userId)).willReturn(responseDto);

        // when & then
        mockMvc.perform(get("/api/v1/users/{userId}", userId)
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.result.id").value(userId));
    }
}
