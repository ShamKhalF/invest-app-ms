package az.shlf.marketdataservice.cache;

import az.shlf.marketdataservice.model.enums.RedisKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisService {

   private final RedisTemplate<String, Object> redisTemplate;
   private final StringRedisTemplate stringRedisTemplate;
   private final ObjectMapper objectMapper;

   public void processLiveStreamData(RedisKeys streamType, String symbol, Object data) {
      if (streamType.getChannelName() == null) {
         log.error("Xəta: Bu RedisKey üçün Pub/Sub kanalı təyin edilməyib: {}", streamType);
         return;
      }

      try {
         String jsonData = objectMapper.writeValueAsString(data);
         String redisKey = streamType.getKeyName() + symbol.toUpperCase();
         stringRedisTemplate.opsForValue().set(redisKey, jsonData, 10, TimeUnit.SECONDS);
         stringRedisTemplate.convertAndSend(streamType.getChannelName(), jsonData);
      } catch (Exception e) {
         log.error("Obyekti JSON-a çevirərkən xəta baş verdi!", e);
      }
   }

   // --- Mövcud Hash və Value Metodları ---
   public void setValue(String key, Object value) { redisTemplate.opsForValue().set(key, value); }
   public Object getValue(String key) { return redisTemplate.opsForValue().get(key); }
   public void deleteValue(String key) { redisTemplate.delete(key); }
   public void setExpire(String key, long timeout, TimeUnit unit) { redisTemplate.expire(key, timeout, unit); }

   public List<Object> multiGetValues(Collection<String> keys) {
      return redisTemplate.opsForValue().multiGet(keys);
   }

   // --- SET ƏMƏLİYYATLARI ---
   public void addToSet(String key, Object value) { redisTemplate.opsForSet().add(key, value); }
   public void removeFromSet(String key, Object value) { redisTemplate.opsForSet().remove(key, value); }
   public Set<Object> getSetMembers(String key) { return redisTemplate.opsForSet().members(key); }

   public void setValueWithExpire(String key, Object value, long timeout, TimeUnit unit) {
      redisTemplate.opsForValue().set(key, value, timeout, unit);
   }

   // --- ZSET ƏMƏLİYYATLARI (YENİ ƏLAVƏ EDİLDİ) ---
   public void incrementZSetScore(String key, String value, double score) {
      redisTemplate.opsForZSet().incrementScore(key, value, score);
   }

   public Set<ZSetOperations.TypedTuple<Object>> getZSetWithScores(String key) {
      return redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1);
   }
}