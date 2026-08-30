package az.shlf.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

   @Bean
   public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
      RedisTemplate<String, Object> template = new RedisTemplate<>();
      template.setConnectionFactory(connectionFactory);

      // Key-ləri təmiz String kimi saxla (Konsolda qəribə simvollar olmasın deyə)
      template.setKeySerializer(RedisSerializer.string());
      template.setHashKeySerializer(RedisSerializer.string());

      // Value-ları və Hash-in içindəki məlumatları JSON formatında saxla
      template.setValueSerializer(RedisSerializer.json());
      template.setHashValueSerializer(RedisSerializer.json());

      template.afterPropertiesSet();
      return template;
   }
}
