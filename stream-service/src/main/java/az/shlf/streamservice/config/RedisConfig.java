package az.shlf.streamservice.config;

import az.shlf.streamservice.listener.RedisStreamListener;
import az.shlf.streamservice.model.enums.StreamType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

   // Pub/Sub üçün dinləyicimiz
   private final RedisStreamListener redisStreamListener;

   // ==========================================
   // 1. ANBAR ÜÇÜN KONFİQURASİYA (Sənin yazdığın)
   // Gələcəkdə Session id-ləri saxlamaq üçün istifadə edəcəyik
   // ==========================================
   @Bean
   public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
      RedisTemplate<String, Object> template = new RedisTemplate<>();
      template.setConnectionFactory(connectionFactory);

      template.setKeySerializer(RedisSerializer.string());
      template.setHashKeySerializer(RedisSerializer.string());
      template.setValueSerializer(RedisSerializer.json());
      template.setHashValueSerializer(RedisSerializer.json());

      template.afterPropertiesSet();
      return template;
   }

   // ==========================================
   // 2. RADİO STANSİYASI ÜÇÜN KONFİQURASİYA (Yeni əlavə)
   // Market Data MS-in kanallarını dinləmək üçün
   // ==========================================

   @Bean
   public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
      RedisMessageListenerContainer container = new RedisMessageListenerContainer();
      container.setConnectionFactory(connectionFactory);

      // Enum vasitəsilə kanalları dinləməyə əlavə edirik
      container.addMessageListener(redisStreamListener, new PatternTopic(StreamType.DEPTH.getChannelName()));
      container.addMessageListener(redisStreamListener, new PatternTopic(StreamType.TICKER.getChannelName()));
      container.addMessageListener(redisStreamListener, new PatternTopic(StreamType.KLINE.getChannelName()));

      return container;
   }

}