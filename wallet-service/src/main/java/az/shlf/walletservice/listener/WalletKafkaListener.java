package az.shlf.walletservice.listener;

import az.shlf.walletservice.listener.dto.WalletCreateEvent;
import az.shlf.walletservice.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class WalletKafkaListener {

   private final WalletService walletService;

   @KafkaListener(topics = "${app.kafka.topics.wallet.name}", groupId = "${spring.kafka.consumer.group-id}")
   public void listenWalletCreateEvent(@Payload WalletCreateEvent event) {
      log.info("WalletCreateEvent qəbul edildi. username: {}", event.getUsername());

      try {
         walletService.createDefaultWallets(event.getUsername());
      } catch (Exception e) {
         log.error("Cüzdan yaradılarkən xəta baş verdi. username: {}, xəta: {}", event.getUsername(), e.getMessage());
         // Zərurət yaranarsa, xəta mesajını Dead Letter Queue (DLQ) topic-inə göndərmək lazımdır.
      }
   }

}