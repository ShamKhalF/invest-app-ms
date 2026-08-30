package az.shlf.apigateway.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static az.shlf.apigateway.constants.redis.RedisKeys.BLACKLIST_PREFIX;

@Service
@RequiredArgsConstructor
public class BlacklistCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Dinamik TTL (Redis-dən gələn nanosaniyəyə əsasən expire olma)
    private final Cache<String, Long> blacklistCache = Caffeine.newBuilder()
            .expireAfter(new Expiry<String, Long>() {
                @Override
                public long expireAfterCreate(String key, Long value, long currentTime) {
                    return value; // Value olaraq TTL-i nanosaniyə ilə verəcəyik
                }

                @Override
                public long expireAfterUpdate(String key, Long value, long currentTime, long currentDuration) {
                    return value;
                }

                @Override
                public long expireAfterRead(String key, Long value, long currentTime, long currentDuration) {
                    return currentDuration;
                }
            })
            .build();

    public boolean isBlacklisted(String jti) {
        // 1. Birinci Caffeine Cache-i yoxla
        if (blacklistCache.getIfPresent(jti) != null) {
            return true;
        }

        // 2. Əgər Cache-də yoxdursa, Redis-i yoxla
        String redisKey = BLACKLIST_PREFIX.getKey() + jti;
        if (redisTemplate.hasKey(redisKey)) {
            
            // 3. Əgər Redis-də varsa, arxa planda (Asynchronously) Caffeine-ə əlavə et
            CompletableFuture.runAsync(() -> {
                Long ttlNanos = redisTemplate.getExpire(redisKey, TimeUnit.NANOSECONDS);
                if (ttlNanos > 0) {
                    // Cache-ə daxil edirik. Value kimi ttlNanos göndəririk ki, Expiry onu oxuya bilsin.
                    blacklistCache.put(jti, ttlNanos);
                }
            });
            return true;
        }

        return false;
    }
}