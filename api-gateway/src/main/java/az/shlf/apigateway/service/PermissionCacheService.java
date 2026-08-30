package az.shlf.apigateway.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import static az.shlf.apigateway.constants.redis.RedisKeys.GROUPED_PERMISSIONS_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionCacheService {

    private final Cache<String, Map<String, Map<String, List<String>>>> permissionsCache;
    private final StringRedisTemplate stringRedisTemplate; // Dəyişdirildi
    private final ObjectMapper objectMapper;

    private static final String CACHE_KEY = "PERMISSIONS";

    @PostConstruct
    public void loadPermissionsFromRedisOnStartup() {
        refreshCacheFromRedis();
    }

    public void refreshCacheFromRedis() {
        try {
            // Məlumat String kimi oxunur
            String redisData = stringRedisTemplate.opsForValue().get(GROUPED_PERMISSIONS_KEY.getKey());

            if (redisData != null && !redisData.isBlank()) {
                // String-dən Obyektə çevirmək üçün readValue istifadə edilir
                Map<String, Map<String, List<String>>> groupedPermissions = objectMapper.readValue(
                        redisData,
                        new TypeReference<>() {}
                );

                permissionsCache.put(CACHE_KEY, groupedPermissions);
                log.info("Permissions cache successfully loaded/updated from Redis");
            } else {
                log.warn("No permissions found in Redis.");
                permissionsCache.invalidate(CACHE_KEY);
            }
        } catch (Exception e) {
            log.error("Error loading permissions from Redis", e);
        }
    }

    public Map<String, Map<String, List<String>>> getPermissions() {
        return permissionsCache.getIfPresent(CACHE_KEY);
    }
}