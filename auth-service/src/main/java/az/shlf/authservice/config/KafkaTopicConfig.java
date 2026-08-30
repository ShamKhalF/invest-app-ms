package az.shlf.authservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    @Value("${app.kafka.topics.mail.name}")
    private String mailTopicName;
    @Value("${app.kafka.topics.mail.partitions}")
    private int mailTopicPartitions;
    @Value("${app.kafka.topics.mail.replicas}")
    private int mailTopicReplicas;

    @Value("${app.kafka.topics.wallet.name}")
    private String walletTopicName;
    @Value("${app.kafka.topics.wallet.partitions}")
    private int walletTopicPartitions;
    @Value("${app.kafka.topics.wallet.replicas}")
    private int walletTopicReplicas;

    // Retention və In-sync parametrləri
    private static final String MIN_INSYNC_REPLICAS = "2";
    private static final String RETENTION_MS = "604800000"; // 1 həftə
    private static final String RETENTION_BYTES = "104857600"; // 100 MB

    @Bean
    public NewTopic mailTopic() {
        return TopicBuilder.name(mailTopicName)
                .partitions(mailTopicPartitions)
                .replicas(mailTopicReplicas)
                .configs(Map.of(
                        "min.insync.replicas", MIN_INSYNC_REPLICAS,
                        "retention.ms", RETENTION_MS,
                        "retention.bytes", RETENTION_BYTES
                ))
                .build();
    }

    @Bean
    public NewTopic walletTopic() {
        return TopicBuilder.name(walletTopicName)
                .partitions(walletTopicPartitions)
                .replicas(walletTopicReplicas)
                .configs(Map.of(
                        "min.insync.replicas", MIN_INSYNC_REPLICAS,
                        "retention.ms", RETENTION_MS,
                        "retention.bytes", RETENTION_BYTES
                ))
                .build();
    }

}