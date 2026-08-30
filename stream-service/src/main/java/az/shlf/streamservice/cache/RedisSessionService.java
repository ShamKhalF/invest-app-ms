package az.shlf.streamservice.cache;

import az.shlf.streamservice.model.enums.RedisChannel;
import az.shlf.streamservice.model.enums.SessionPrefix;
import az.shlf.streamservice.model.enums.StreamType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisSessionService {

   private final RedisTemplate<String, Object> redisTemplate;
   private final StringRedisTemplate stringRedisTemplate;
   private final ObjectMapper objectMapper;

   // 1. İstifadəçini otağa və profilinə əlavə edir
   public void addUserToRoom(String destination, String sessionId) {
      redisTemplate.opsForSet().add(SessionPrefix.ROOM.getPrefix() + destination, sessionId);
      redisTemplate.opsForSet().add(SessionPrefix.USER.getPrefix() + sessionId, destination);
   }

   // 2. İstifadəçinin hansı otaqlarda olduğunu qaytarır
   public Set<Object> getUserRooms(String sessionId) {
      return redisTemplate.opsForSet().members(SessionPrefix.USER.getPrefix() + sessionId);
   }

   // 3. İstifadəçini tək bir otaqdan silir
   public void removeUserFromRoom(String room, String sessionId) {
      redisTemplate.opsForSet().remove(SessionPrefix.ROOM.getPrefix() + room, sessionId);
   }

   // 4. Otaqda qalan adam sayını qaytarır
   public Long getRoomUserCount(String room) {
      return redisTemplate.opsForSet().size(SessionPrefix.ROOM.getPrefix() + room);
   }

   // 5. İstifadəçinin profilini tamamilə silir
   public void clearUser(String sessionId) {
      redisTemplate.delete(SessionPrefix.USER.getPrefix() + sessionId);
   }


   // --- Market Stream Simvollarının İdarəedilməsi ---
   private String getStreamKey(StreamType streamType) {
      return "ACTIVE_SYMBOLS:" + streamType.name();
   }

   public boolean addSymbolToStream(StreamType streamType, String symbol) {
      Long result = redisTemplate.opsForSet().add(getStreamKey(streamType), symbol);
      return result != null && result > 0;
   }

   public boolean removeSymbolFromStream(StreamType streamType, String symbol) {
      Long result = redisTemplate.opsForSet().remove(getStreamKey(streamType), symbol);
      return result != null && result > 0;
   }

   public Set<String> getStreamSymbols(StreamType streamType) {
      Set<Object> members = redisTemplate.opsForSet().members(getStreamKey(streamType));
      if (members == null || members.isEmpty()) {
         return new HashSet<>();
      }
      return members.stream()
              .map(Object::toString)
              .collect(Collectors.toSet());
   }

   // --- YENİ: Pub/Sub Yayım Metodu ---
   public void publishActiveSymbols(StreamType streamType, Set<String> activeSymbols) {
      RedisChannel targetChannel = RedisChannel.fromStreamType(streamType);
      try {
         String jsonData = objectMapper.writeValueAsString(activeSymbols);
         stringRedisTemplate.convertAndSend(targetChannel.getChannelName(), jsonData);

         log.info("📡 [REDIS PUB/SUB] YENİ VƏZİYYƏT YAYIMLANDI | Channel: {} | Siyahı: {}",
                 targetChannel.getChannelName(), jsonData);
      } catch (Exception e) {
         log.error("Siyahını JSON-a çevirərkən xəta baş verdi!", e);
      }
   }

}
