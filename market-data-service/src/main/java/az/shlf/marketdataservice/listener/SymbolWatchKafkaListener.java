package az.shlf.marketdataservice.listener;

import az.shlf.marketdataservice.cache.RedisService;
import az.shlf.marketdataservice.dto.SymbolWatchEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SymbolWatchKafkaListener {

   private final RedisService redisService;
   private static final String TOP_SYMBOLS_ZSET_KEY = "top_watched_symbols_zset";

   @KafkaListener(topics = "symbol-watch-topic", groupId = "market-data-group")
   public void listenSymbolWatchEvent(SymbolWatchEvent event) {
      if (event != null && event.getSymbol() != null) {
         String symbol = event.getSymbol().toUpperCase().trim();
         redisService.incrementZSetScore(TOP_SYMBOLS_ZSET_KEY, symbol, 1);
         log.info("Kafka-dan simvol izlənməsi qəbul edildi və Redis-də artırıldı: {}", symbol);
      }
   }
}