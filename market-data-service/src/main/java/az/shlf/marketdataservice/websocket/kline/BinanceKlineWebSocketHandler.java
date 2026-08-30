package az.shlf.marketdataservice.websocket.kline;

import az.shlf.marketdataservice.cache.RedisService;
import az.shlf.marketdataservice.model.enums.RedisKeys;
import az.shlf.marketdataservice.model.wss.LiveKlineResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinanceKlineWebSocketHandler extends TextWebSocketHandler {

   private final ObjectMapper objectMapper;
   private final RedisService redisService;

   @Override
   protected void handleTextMessage(WebSocketSession session, TextMessage message) {
      String payload = message.getPayload();

      try {
         JsonNode rootNode = objectMapper.readTree(payload);
         JsonNode dataNode = rootNode.has("data") ? rootNode.get("data") : rootNode;

         // Kline datası 'k' obyektinin içində gəlir
         if (!dataNode.has("k")) {
            return;
         }

         JsonNode klineNode = dataNode.get("k");

         // Məlumatları çəkirik
         String symbol = klineNode.get("s").asText();
         String interval = klineNode.get("i").asText();
         Long startTime = klineNode.get("t").asLong();
         Long endTime = klineNode.get("T").asLong();
         String openPrice = klineNode.get("o").asText();
         String closePrice = klineNode.get("c").asText(); // Canlı rəqs edən qiymət
         String highPrice = klineNode.get("h").asText();
         String lowPrice = klineNode.get("l").asText();
         String volume = klineNode.get("v").asText();
         boolean isClosed = klineNode.get("x").asBoolean();

         LiveKlineResponse response = new LiveKlineResponse(
                 symbol, interval, startTime, endTime,
                 openPrice, closePrice, highPrice, lowPrice, volume, isClosed
         );

//         // Redis-ə yazırıq (Məsələn: LIVE_KLINE:BTCUSDT:1m)
//         String redisKey = RedisKeys.LIVE_KLINE.getKeyName() + ":" + symbol + ":" + interval;
//         // TTL 10 saniyə (Gələcəkdə qırılma olsa ekranda donuq qalmasın)
//         redisService.setValueWithExpire(redisKey, response, 10, TimeUnit.SECONDS);

         // Məlumatı həm 2 saniyəlik anbara yazır, həm də canlı (Pub/Sub) kanala atır
         redisService.processLiveStreamData(RedisKeys.LIVE_KLINE, symbol, response);

         // Əgər şam qapandısa (x: true), ayrıca loglaya və ya xüsusi event ata bilərik
         if (isClosed) {
            log.info("🔴 ŞAM QAPANDI! Koin: {}, İnterval: {}, Qapanış Qiyməti: {}", symbol, interval, closePrice);
            // Gələcəkdə burada Kafka-ya ayrıca xəbər ata bilərsən ki: "Frontend-ə de yeni şam çəksin!"
         }

      } catch (Exception e) {
         log.error("Error parsing Binance Kline WSS message", e);
      }
   }

   @Override
   public void afterConnectionEstablished(WebSocketSession session) {
      log.info("Binance KLINE WebSocket connected! Session ID: {}", session.getId());
   }

   @Override
   public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
      log.warn("Binance KLINE WebSocket disconnected! Status: {}", status);
   }
}