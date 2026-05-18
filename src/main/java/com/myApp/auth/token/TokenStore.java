package com.myApp.auth.token;

import java.util.Optional;

public interface TokenStore {

    void saveRefreshToken(String id, String token);

    Optional<String> findRefreshToken(String id);

    void deleteRefreshToken(String id);

    void blacklistAccessToken(String accessToken, long expirationMillis);

    boolean isAccessTokenBlacklisted(String accessToken);
}
