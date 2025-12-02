package com.myApp.auth.service;

import com.myApp.auth.entity.Role;
import com.myApp.auth.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String name;
    private String email;
    private String picture;
    private String socialType;
    private String socialId;

    @Builder
    public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name, String email,
                           String picture, String socialType, String socialId) {
        this.attributes = attributes;
        this.nameAttributeKey = nameAttributeKey;
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.socialType = socialType;
        this.socialId = socialId;
    }

    public static OAuthAttributes of(String registrationId, String userNameAttributeName,
                                     Map<String, Object> attributes) {

        return switch (registrationId) {
            case "google" -> ofGoogle(userNameAttributeName, attributes);
            case "naver" -> ofNaver(userNameAttributeName, attributes);
            case "kakao" -> ofKakao(userNameAttributeName, attributes);
            default -> null;
        };
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .name((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .picture((String) attributes.get("picture"))
                .socialType("google")
                .socialId((String) attributes.get("sub"))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        return OAuthAttributes.builder()
                .name((String) response.get("name"))
                .email((String) response.get("email"))
                .picture((String) response.get("profile_image"))
                .socialType("naver")
                .socialId((String) response.get("id"))
                .attributes(response)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuthAttributes.builder()
                .name((String) profile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .picture((String) profile.get("profile_image_url"))
                .socialType("kakao")
                .socialId(String.valueOf(attributes.get("id")))
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    public User toEntity() {
        return User.builder()
                .name(name)
                .email(email)
                .picture(picture)
                .role(Role.USER)
                .socialType(socialType)
                .socialId(socialId)
                .build();
    }
}
