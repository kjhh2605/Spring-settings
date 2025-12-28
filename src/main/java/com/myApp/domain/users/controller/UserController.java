package com.myApp.domain.users.controller;

import com.myApp.auth.annotation.AuthUser;
import com.myApp.auth.annotation.CheckBlacklist;
import com.myApp.domain.users.dto.UserResponseDto;
import com.myApp.domain.users.service.UserService;
import com.myApp.global.apiPayload.ApiResponse;
import com.myApp.global.apiPayload.code.status.GeneralSuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController implements UserControllerDocs{

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponseDto> getMyInfo(@AuthUser UserDetails userDetails) {
        UserResponseDto myInfo = userService.getMyInfo(userDetails.getUsername());
        return ApiResponse.onSuccess(GeneralSuccessCode._OK, myInfo);
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserResponseDto> getUser(@PathVariable Long userId) {
        UserResponseDto userInfo = userService.getUser(userId);
        return ApiResponse.onSuccess(GeneralSuccessCode._OK, userInfo);
    }
}
