package az.shlf.mailservice.listener;

import az.shlf.mailservice.dto.EmailEvent;
import az.shlf.mailservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailKafkaListener {

   private final EmailService emailService;

   @KafkaListener(topics = "${app.kafka.topics.mail.name}",
           groupId = "mail-service-group",
           properties = {"spring.json.value.default.type=az.shlf.mailservice.dto.EmailEvent"})
   public void consumeMailEvent(EmailEvent event) {
      log.info("Kafka-dan email eventi qəbul edildi: {}, tip: {}", event.getEmail(), event.getType());
      emailService.sendEmail(event);
   }

}