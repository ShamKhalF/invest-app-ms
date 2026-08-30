//package az.shlf.telegramaibot.service;
//
//import az.shlf.telegramaibot.dto.CryptoContentDto;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class MarketDataKafkaConsumer {
//
//   private final RagVectorStoreService ragVectorStoreService;
//
//   @KafkaListener(topics = "market-data-hourly-klines",
//           groupId = "telegram-ai-bot-group",
//           containerFactory = "kafkaListenerContainerFactory")
//   public void consumeMarketData(CryptoContentDto klineDto) {
//      log.info("Kafka-dan məlumat oxundu: {}", klineDto.symbol());
//
//      try {
//         ragVectorStoreService.saveKlineDataToRag(klineDto);
//      } catch (Exception e) {
//         log.error("RAG sisteminə yazılarkən xəta baş verdi. Koin: {}. Xəta: {}", klineDto.symbol(), e.getMessage());
//         // Zərurət yaranarsa, burada DLQ (Dead Letter Queue) məntiqi icra edilə bilər
//      }
//   }
//
//}