package com.captain.authservice.service;



import com.captain.authservice.config.JwtProperties;
import com.captain.authservice.exception.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    private static final String KEY_PREFIX = "refresh:";

    public void saveToken(String rawToken, UUID userId) {
        String key = buildKey(userId, rawToken);
        Duration ttl = Duration.ofMillis(jwtProperties.getRefreshTokenExpiry());
        redisTemplate.opsForValue().set(key, "active", ttl);
        log.debug("Refresh token stored in Redis for user: {}", userId);
    }

    public void validateAndRevoke(String rawToken, UUID userId) {
        String key = buildKey(userId, rawToken);

        Boolean exists = redisTemplate.hasKey(key);
        if (!exists) {
            log.warn("Refresh token not found in Redis for user: {}. Possible reuse attack.", userId);
            revokeAllUserTokens(userId);
            throw AuthException.tokenRevoked();
        }

        redisTemplate.delete(key);
        log.debug("Refresh token rotated for user: {}", userId);
    }

    public void revokeAllUserTokens(UUID userId) {
        String pattern = KEY_PREFIX + userId.toString() + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Revoked {} refresh tokens for user: {}", keys.size(), userId);
        }
    }

    private String buildKey(UUID userId, String rawToken) {
        String tokenHash = hashToken(rawToken);
        return KEY_PREFIX + userId.toString() + ":" + tokenHash;
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
