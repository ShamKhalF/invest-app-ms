package az.shlf.authservice.service.impl;

import az.shlf.authservice.service.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

   private final RedisTemplate<String, String> redisTemplate;
   private final ObjectMapper objectMapper;

   @Override
   @SneakyThrows
   public <T> T get(String key, Class<T> type) {
      String jsonValue = redisTemplate.opsForValue().get(key);
      if (jsonValue == null) {
         return null;
      }
      return objectMapper.readValue(jsonValue, type);
   }

   @Override
   @SneakyThrows
   public <T> void set(String key, T value) {
      String jsonValue = objectMapper.writeValueAsString(value);
      redisTemplate.opsForValue().set(key, jsonValue);
   }

   @Override
   @SneakyThrows
   public <T> void set(String key, T value, long timeout, TimeUnit unit) {
      String jsonValue = objectMapper.writeValueAsString(value);
      Duration duration = Duration.of(timeout, unit.toChronoUnit());
      redisTemplate.opsForValue().set(key, jsonValue, duration);
   }

   @Override
   public void delete(String key) {
      redisTemplate.delete(key);
   }

   @Override
   public void publish(String channel, String message) {
      redisTemplate.convertAndSend(channel, message);
   }

   @Override
   public Long increment(String key) {
      return redisTemplate.opsForValue().increment(key);
   }

   @Override
   public Boolean expire(String key, long timeout, TimeUnit unit) {
      Duration duration = Duration.of(timeout, unit.toChronoUnit());
      return redisTemplate.expire(key, duration);
   }

}
