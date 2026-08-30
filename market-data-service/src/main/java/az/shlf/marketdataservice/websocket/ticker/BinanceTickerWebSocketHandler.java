package az.shlf.marketdataservice.websocket.ticker;

import az.shlf.marketdataservice.cache.RedisService;
import az.shlf.marketdataservice.model.enums.RedisKeys;
import az.shlf.marketdataservice.model.wss.LiveTickerResponse;
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
public class BinanceTickerWebSocketHandler extends TextWebSocketHandler {

   private final ObjectMapper objectMapper;
   private final RedisService redisService;

   @Override
   protected void handleTextMessage(WebSocketSession session, TextMessage message) {
      String payload = message.getPayload();

      try {
         JsonNode rootNode = objectMapper.readTree(payload);
         JsonNode dataNode = rootNode;

         // 2. Əgər Multiplexing (Çoxlu koin) stream-idirsə, əsas data "data" obyektinin içində olur
         if (rootNode.has("data")) {
            dataNode = rootNode.get("data");
         }

         // 3. Əgər gələn JSON-un içində 's' (symbol), 'c' (current price) və 'P' (percent) yoxdursa,
         // deməli bu sadəcə qoşulma təsdiqi mesajıdır, iqnor edirik.
         if (!dataNode.has("s") || !dataNode.has("c") || !dataNode.has("P")) {
            return;
         }

         // 4. Dataları çəkirik
         String symbol = dataNode.get("s").asText();
         String currentPrice = dataNode.get("c").asText();
         String priceChangePercent = dataNode.get("P").asText();

         // 5. Öz DTO obyektimizi yaradırıq
         LiveTickerResponse response = new LiveTickerResponse(symbol, currentPrice, priceChangePercent);

//         // 6. Redis-ə yazırıq!
//         // Qeyd: TTL 5 saniyə təyin edirik ki, əgər nə vaxtsa WSS əlaqəsi qırılarsa,
//         // ekranda köhnə (donmuş) qiymət asılı qalmasın, dərhal silinsin.
//         String redisKey = RedisKeys.LIVE_TICKER.getKeyName() + ":" + symbol;
//         redisService.setValueWithExpire(redisKey, response, 10, TimeUnit.SECONDS);

         // Məlumatı həm 2 saniyəlik anbara yazır, həm də canlı (Pub/Sub) kanala atır
         redisService.processLiveStreamData(RedisKeys.LIVE_TICKER, symbol, response);

         // Çox sürətli gəldiyi üçün log.info əvəzinə log.debug yazmaq məsləhətdir (konsolu doldurmasın deyə)
//         log.debug("Live Ticker Updated in Redis: {} -> {}", symbol, currentPrice);
         log.info("Canlı Qiymət Gəldi! Koin: {} -> Qiymət: {}", symbol, currentPrice);
      } catch (Exception e) {
         log.error("Error parsing Binance WSS message: {}", payload, e);
      }
   }

   @Override
   public void afterConnectionEstablished(WebSocketSession session) {
      log.info("Successfully connected to Binance WebSocket! Session ID: {}", session.getId());
   }

   @Override
   public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
      log.warn("Binance WebSocket disconnected! Status: {}", status);
   }

}