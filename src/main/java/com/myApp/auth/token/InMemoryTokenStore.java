package com.myApp.auth.token;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Profile("dev")
public class InMemoryTokenStore implements TokenStore {

    private final Map<String, String> refreshTokens = new ConcurrentHashMap<>();
    private final Map<String, Long> blacklistedAccessTokens = new ConcurrentHashMap<>();

    @Override
    public void saveRefreshToken(String id, String token) {
        refreshTokens.put(id, token);
    }

    @Override
    public Optional<String> findRefreshToken(String id) {
        return Optional.ofNullable(refreshTokens.get(id));
    }

    @Override
    public void deleteRefreshToken(String id) {
        refreshTokens.remove(id);
    }

    @Override
    public void blacklistAccessToken(String accessToken, long expirationMillis) {
        if (expirationMillis <= 0) {
            return;
        }
        blacklistedAccessTokens.put(accessToken, Instant.now().toEpochMilli() + expirationMillis);
    }

    @Override
    public boolean isAccessTokenBlacklisted(String accessToken) {
        Long expiresAt = blacklistedAccessTokens.get(accessToken);
        if (expiresAt == null) {
            return false;
        }

        if (expiresAt <= Instant.now().toEpochMilli()) {
            blacklistedAccessTokens.remove(accessToken);
            return false;
        }

        return true;
    }
}
