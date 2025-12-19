package com.myApp.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myApp.auth.handler.OAuth2SuccessHandler;
import com.myApp.auth.jwt.JwtAuthenticationFilter;
import com.myApp.auth.jwt.JwtTokenProvider;
import com.myApp.auth.service.CustomOAuth2UserService;
import com.myApp.global.apiPayload.ApiResponse;
import com.myApp.global.apiPayload.code.status.AuthErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // 1. 공통 정적 리소스 및 H2 콘솔
                        .requestMatchers("/", "/css/**", "/images/**", "/js/**", "/favicon.ico", "/h2-console/**").permitAll()

                        // 2. Swagger UI (개발 환경)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 3. 인증/인가 관련 엔드포인트
                        .requestMatchers("/api/v1/auth/**", "/oauth2/**", "/login/oauth2/**").permitAll()

                        // 4. S3 관련
                        .requestMatchers("/api/v1/s3/**").permitAll()

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated())

                // 인증 실패 시 401 JSON 응답 반환
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(AuthErrorCode.AUTH_UNAUTHORIZED.getHttpStatus().value());

                            ApiResponse.Body<?> errorBody = ApiResponse.createFailureBody(AuthErrorCode.AUTH_UNAUTHORIZED);

                            ObjectMapper objectMapper = new ObjectMapper();
                            objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                            response.getWriter().write(objectMapper.writeValueAsString(errorBody));
                        }))

                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler))

                // JWT 필터 배치
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
