package com.myApp.auth.token;

import com.myApp.auth.redis.RefreshToken;
import com.myApp.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Profile("!dev")
@RequiredArgsConstructor
public class RedisTokenStore implements TokenStore {

    private final RefreshTokenRepository refreshTokenRepository;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void saveRefreshToken(String id, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .id(id)
                .token(token)
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    @Override
    public Optional<String> findRefreshToken(String id) {
        return refreshTokenRepository.findById(id)
                .map(RefreshToken::getToken);
    }

    @Override
    public void deleteRefreshToken(String id) {
        refreshTokenRepository.deleteById(id);
    }

    @Override
    public void blacklistAccessToken(String accessToken, long expirationMillis) {
        if (expirationMillis <= 0) {
            return;
        }

        redisTemplate.opsForValue()
                .set("blacklist:" + accessToken, "logout", expirationMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isAccessTokenBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("blacklist:" + accessToken));
    }
}
