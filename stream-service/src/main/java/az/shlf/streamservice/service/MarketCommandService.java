package az.shlf.streamservice.service;

import az.shlf.streamservice.cache.RedisSessionService;
import az.shlf.streamservice.model.enums.StreamType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketCommandService {

   private final RedisSessionService redisSessionService;

   public void updateStreamState(String streamTypeStr, String symbol, boolean isAdding) {

      StreamType streamType = StreamType.fromTopicName(streamTypeStr);
      boolean isChanged;

      // 1. Məlumatı Redis Set-də yeniləyirik
      if (isAdding) {
         isChanged = redisSessionService.addSymbolToStream(streamType, symbol);
      } else {
         isChanged = redisSessionService.removeSymbolFromStream(streamType, symbol);
      }

      // 2. Əgər doğrudan da dəyişiklik oldusa, yeni vəziyyəti alıb yayım edirik
      if (isChanged) {
         Set<String> activeSymbols = redisSessionService.getStreamSymbols(streamType);
         redisSessionService.publishActiveSymbols(streamType, activeSymbols);
      }
   }

//   private final KafkaTemplate<String, Object> kafkaTemplate;
//
//   private final Set<String> activeDepthSymbols = ConcurrentHashMap.newKeySet();
//   private final Set<String> activeTickerSymbols = ConcurrentHashMap.newKeySet();
//   private final Set<String> activeKlineSymbols = ConcurrentHashMap.newKeySet();
//
//   public void updateStreamState(String streamTypeStr, String symbol, boolean isAdding) {
//
//      StreamType streamType = StreamType.fromTopicName(streamTypeStr);
//      Set<String> targetSet = getSetByStreamType(streamType);
//
//      boolean isChanged;
//
//      if (isAdding) {
//         isChanged = targetSet.add(symbol);
//      } else {
//         isChanged = targetSet.remove(symbol);
//      }
//
//      if (isChanged) {
//         // Multi-topic dizaynında birbaşa təmiz Set göndərilir
//         Set<String> symbolsCopy = new HashSet<>(targetSet);
//
//         // Dinamik olaraq hədəf mövzunu seçirik
//         KafkaTopic targetTopic = KafkaTopic.fromStreamType(streamType);
//
//         kafkaTemplate.send(targetTopic.getTopicName(), symbolsCopy);
//         log.info("📡 [KAFKA] YENİ VƏZİYYƏT GÖNDƏRİLDİ | Topic: {} | Siyahı: {}", targetTopic.getTopicName(), symbolsCopy);
//      }
//
//   }
//
//   private Set<String> getSetByStreamType(StreamType streamType) {
//      return switch (streamType) {
//         case DEPTH  -> activeDepthSymbols;
//         case TICKER -> activeTickerSymbols;
//         case KLINE  -> activeKlineSymbols;
//      };
//   }

}


//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//@Slf4j
//@Service
//public class MarketCommandService {
//
//   public void triggerStartCommand(String streamType, String symbol) {
//
//      log.warn("======================================================");
//      log.warn("📢 [MANUAL TƏTİKLƏMƏ - START]");
//      log.warn("İlk istifadəçi '{}' otağına qoşuldu!", symbol.toUpperCase());
//      log.warn("Zəhmət olmasa Market Data MS-də {} axınını işə salın.", streamType.toUpperCase());
//      log.warn("======================================================");
//
//      // Əgər gələcəkdə bunu Kafka yox, birbaşa REST API ilə etmək istəsən:
//      // restTemplate.postForLocation("http://localhost:8081/api/market-data/start?stream=" + streamType + "&symbol=" + symbol, null);
//   }
//
//   public void triggerStopCommand(String streamType, String symbol) {
//
//      log.warn("======================================================");
//      log.warn("🛑 [MANUAL TƏTİKLƏMƏ - STOP]");
//      log.warn("'{}' otağındakı sonuncu istifadəçi də sistemdən ayrıldı!", symbol.toUpperCase());
//      log.warn("Resurslara qənaət etmək üçün Market Data MS-də {} axınını dayandırın.", streamType.toUpperCase());
//      log.warn("======================================================");
//
//      // Əgər gələcəkdə bunu Kafka yox, birbaşa REST API ilə etmək istəsən:
//      // restTemplate.postForLocation("http://localhost:8081/api/market-data/stop?stream=" + streamType + "&symbol=" + symbol, null);
//   }
//}