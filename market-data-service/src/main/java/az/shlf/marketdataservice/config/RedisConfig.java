package az.shlf.marketdataservice.config;

import az.shlf.marketdataservice.listener.MarketDataRedisListener;
import az.shlf.marketdataservice.model.enums.RedisChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
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

   @Bean
   public RedisMessageListenerContainer redisMessageListenerContainer(
           RedisConnectionFactory connectionFactory,
           MarketDataRedisListener messageListener) {

      RedisMessageListenerContainer container = new RedisMessageListenerContainer();
      container.setConnectionFactory(connectionFactory);

      // Bütün 3 kanalı eyni listener-ə bağlayırıq
      container.addMessageListener(messageListener, new ChannelTopic(RedisChannel.TICKER_CHANNEL.getChannelName()));
      container.addMessageListener(messageListener, new ChannelTopic(RedisChannel.DEPTH_CHANNEL.getChannelName()));
      container.addMessageListener(messageListener, new ChannelTopic(RedisChannel.KLINE_CHANNEL.getChannelName()));

      return container;
   }

}