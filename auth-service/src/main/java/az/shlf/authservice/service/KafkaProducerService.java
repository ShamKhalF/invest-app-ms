package az.shlf.authservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.topics.mail.name}")
    private String mailTopic;

    @Value("${app.kafka.topics.wallet.name}")
    private String walletTopic;

    public void sendMailEvent(Object mailEvent) {
        log.debug("Sending mail event to topic {}: {}", mailTopic, mailEvent);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(mailTopic, mailEvent);

        future.whenComplete((result, exception) -> {
            if (exception == null) {
                log.info("Mail event successfully sent to topic: {} with offset: {}",
                        mailTopic, result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send mail event to topic: {} due to: {}",
                        mailTopic, exception.getMessage(), exception);
            }
        });
    }

    public void sendWalletEvent(String userId, Object walletEvent) {
        log.debug("Sending wallet event to topic {}: {}", walletTopic, walletEvent);

        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(walletTopic, userId, walletEvent);

        future.whenComplete((result, exception) -> {
            if (exception == null) {
                log.info("Wallet event successfully sent for userId: {} to topic: {} with offset: {}",
                        userId, walletTopic, result.getRecordMetadata().offset());
            } else {
                log.error("Failed to send wallet event for userId: {} to topic: {} due to: {}",
                        userId, walletTopic, exception.getMessage(), exception);
            }
        });
    }

}