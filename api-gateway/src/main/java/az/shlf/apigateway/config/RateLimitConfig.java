package az.shlf.apigateway.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

   @Value("${spring.data.redis.host:localhost}")
   private String redisHost;

   @Value("${spring.data.redis.port:6379}")
   private int redisPort;

   @Bean(destroyMethod = "shutdown")
   public RedisClient redisClient() {
      return RedisClient.create("redis://" + redisHost + ":" + redisPort);
   }

   @Bean
   public ProxyManager<String> proxyManager(RedisClient redisClient) {
      StatefulRedisConnection<String, byte[]> connection = redisClient.connect(
              RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
      );

      ClientSideConfig clientSideConfig = ClientSideConfig.getDefault()
              .withExpirationAfterWriteStrategy(
                      ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(1))
              );

      return LettuceBasedProxyManager.builderFor(connection)
              .withClientSideConfig(clientSideConfig)
              .build();
   }
}