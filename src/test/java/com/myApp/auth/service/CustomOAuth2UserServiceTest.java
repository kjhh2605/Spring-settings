package com.myApp.auth.service;

import com.myApp.auth.entity.Role;
import com.myApp.auth.entity.User;
import com.myApp.auth.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;

    @Test
    @DisplayName("소셜 로그인 시 기존 회원이 없으면 새로 생성한다.")
    void loadUser_NewUser() {
        // given
        customOAuth2UserService.setDelegate(delegate); // Setter Injection

        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId("google")
                .clientId("clientId")
                .clientSecret("clientSecret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("redirectUri")
                .scope("email", "profile")
                .authorizationUri("authorizationUri")
                .tokenUri("tokenUri")
                .userInfoUri("userInfoUri")
                .userNameAttributeName("sub")
                .clientName("Google")
                .build();

        OAuth2UserRequest userRequest = new OAuth2UserRequest(clientRegistration, "accessToken");

        Map<String, Object> attributes = Map.of(
                "sub", "123456789",
                "name", "Test User",
                "email", "test@example.com",
                "picture", "http://example.com/pic.jpg");
        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "sub");

        given(delegate.loadUser(any(OAuth2UserRequest.class))).willReturn(oAuth2User);
        given(userRepository.findByEmail("test@example.com")).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        // then
        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }
}
