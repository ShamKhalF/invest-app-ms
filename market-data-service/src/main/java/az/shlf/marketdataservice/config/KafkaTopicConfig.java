package az.shlf.marketdataservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

@Configuration
public class KafkaTopicConfig {

   @Value("${app.kafka.topics.hourly-kline.name}")
   private String hourlyKlineTopicName;
   @Value("${app.kafka.topics.hourly-kline.partitions}")
   private int hourlyKlineTopicPartitions;
   @Value("${app.kafka.topics.hourly-kline.replicas}")
   private int hourlyKlineTopicReplicas;

   // Retention və In-sync parametrləri
   private static final String MIN_INSYNC_REPLICAS = "2";
   private static final String RETENTION_MS = "604800000"; // 1 həftə
   private static final String RETENTION_BYTES = "104857600"; // 100 MB

   @Bean
   public NewTopic hourlyKlineTopic() {
      return TopicBuilder.name(hourlyKlineTopicName)
              .partitions(hourlyKlineTopicPartitions)
              .replicas(hourlyKlineTopicReplicas)
              .configs(Map.of(
                      "min.insync.replicas", MIN_INSYNC_REPLICAS,
                      "retention.ms", RETENTION_MS,
                      "retention.bytes", RETENTION_BYTES
              ))
              .build();
   }

}