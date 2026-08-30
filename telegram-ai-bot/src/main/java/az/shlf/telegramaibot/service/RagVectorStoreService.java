//package az.shlf.telegramaibot.service;
//
//import az.shlf.telegramaibot.dto.CryptoContentDto;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.ai.document.Document;
//import org.springframework.ai.vectorstore.VectorStore;
//import org.springframework.stereotype.Service;
//
//import java.nio.charset.StandardCharsets;
//import java.time.Instant;
//import java.time.ZoneId;
//import java.time.format.DateTimeFormatter;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class RagVectorStoreService {
//
//   private final VectorStore vectorStore;
//
//   private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
//           .ofPattern("yyyy-MM-dd HH:mm:ss")
//           .withZone(ZoneId.of("UTC"));
//
//   public void saveKlineDataToRag(CryptoContentDto klineDto) {
//      String openTimeStr = DATE_FORMATTER.format(Instant.ofEpochMilli(klineDto.openTime()));
//      String closeTimeStr = DATE_FORMATTER.format(Instant.ofEpochMilli(klineDto.closeTime()));
//
//      String content = String.format(
//              "Kriptovalyuta: %s. Vaxt aralığı: %s - %s (UTC). " +
//                      "Açılış qiyməti: %s, Maksimum qiymət: %s, Minimum qiymət: %s, Bağlanış qiyməti: %s. " +
//                      "Ticarət həcmi: %s.",
//              klineDto.symbol(),
//              openTimeStr,
//              closeTimeStr,
//              klineDto.openPrice().toPlainString(),
//              klineDto.highPrice().toPlainString(),
//              klineDto.lowPrice().toPlainString(),
//              klineDto.closePrice().toPlainString(),
//              klineDto.volume().toPlainString()
//      );
//
//      Map<String, Object> metadata = Map.of(
//              "symbol", klineDto.symbol(),
//              "openTime", klineDto.openTime(),
//              "closeTime", klineDto.closeTime()
//      );
//
//      // 1. Unikal mətni yaradın
//      String uniqueString = String.format("%s-%d", klineDto.symbol(), klineDto.closeTime());
//
//      // 2. Mətni deterministik UUID-yə çevirin
//      String documentId = UUID.nameUUIDFromBytes(uniqueString.getBytes(StandardCharsets.UTF_8)).toString();
//
//      Document document = new Document(documentId, content, metadata);
//
//      vectorStore.add(List.of(document));
//      log.info("RAG VectorStore-a uğurla yazıldı: {}", klineDto.symbol());
//   }
//
//
//
//}