package com.myApp.auth.aspect;

import com.myApp.auth.token.TokenStore;
import com.myApp.global.apiPayload.code.status.AuthErrorCode;
import com.myApp.global.apiPayload.exception.GeneralException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class BlacklistAspect {

    private final TokenStore tokenStore;

    @Before("@annotation(com.myApp.auth.annotation.CheckBlacklist)")
    public void checkBlacklist() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String accessToken = bearerToken.substring(7);

            if (tokenStore.isAccessTokenBlacklisted(accessToken)) {
                throw new GeneralException(AuthErrorCode.AUTH_TOKEN_INVALID);
            }
        }
    }
}
