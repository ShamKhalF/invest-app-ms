package az.shlf.streamservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

   @Value("${app.kafka.topics.symbol.name}")
   private String symbolTopicName;
   @Value("${app.kafka.topics.symbol.partitions}")
   private int symbolTopicPartitions;
   @Value("${app.kafka.topics.symbol.replicas}")
   private int symbolTopicReplicas;

   // Retention və In-sync parametrləri
   private static final String MIN_INSYNC_REPLICAS = "2";
   private static final String RETENTION_MS = "604800000"; // 1 həftə
   private static final String RETENTION_BYTES = "104857600"; // 100 MB

   @Bean
   public NewTopic symbolTopic() {
      return TopicBuilder.name(symbolTopicName)
              .partitions(symbolTopicPartitions)
              .replicas(symbolTopicReplicas)
              .configs(Map.of(
                      "min.insync.replicas", MIN_INSYNC_REPLICAS,
                      "retention.ms", RETENTION_MS,
                      "retention.bytes", RETENTION_BYTES
              ))
              .build();
   }


   @Value("${spring.kafka.bootstrap-servers}")
   private String bootstrapServers;

   @Bean
   public ProducerFactory<String, Object> producerFactory(ObjectMapper objectMapper) {
      Map<String, Object> configProps = new HashMap<>();
      configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

      // Key üçün standart String Serializer
      Serializer<String> keySerializer = new StringSerializer();

      // Value üçün mövcud ObjectMapper-dən istifadə edən Custom JSON Serializer (Deprecated olanı əvəz edir)
      Serializer<Object> valueSerializer = (topic, data) -> {
         try {
            return data == null ? null : objectMapper.writeValueAsBytes(data);
         } catch (Exception e) {
            throw new RuntimeException("Error serializing message to JSON", e);
         }
      };

      return new DefaultKafkaProducerFactory<>(configProps, keySerializer, valueSerializer);
   }

   @Bean
   public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
      return new KafkaTemplate<>(producerFactory);
   }

}