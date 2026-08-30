package az.shlf.marketdataservice.websocket.depth;

import az.shlf.marketdataservice.cache.RedisService;
import az.shlf.marketdataservice.model.enums.RedisKeys;
import az.shlf.marketdataservice.model.wss.DepthEntry;
import az.shlf.marketdataservice.model.wss.LiveDepthResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BinanceDepthWebSocketHandler extends TextWebSocketHandler {

   private final ObjectMapper objectMapper;
   private final RedisService redisService;

   @Override
   protected void handleTextMessage(WebSocketSession session, TextMessage message) {
      try {
         JsonNode rootNode = objectMapper.readTree(message.getPayload());
         JsonNode dataNode = rootNode.has("data") ? rootNode.get("data") : rootNode;

         String symbol;

         // Koinin adını tapırıq. Multiplexing-də stream adından (məs: btcusdt@depth20@100ms) kəsib alırıq.
         if (rootNode.has("stream")) {
            symbol = rootNode.get("stream").asText().split("@")[0].toUpperCase();
         } else if (dataNode.has("s")) {
            symbol = dataNode.get("s").asText(); // Diff depth üçündür (əgər gələcəkdə lazım olsa)
         } else {
            return; // Əgər heç biri yoxdursa, qoşulma mesajıdır, keçirik.
         }

         // Bids və Asks array-lərini oxuyuruq
         List<DepthEntry> bids = parseDepthArray(dataNode, "bids", "b");
         List<DepthEntry> asks = parseDepthArray(dataNode, "asks", "a");

         LiveDepthResponse response = new LiveDepthResponse(symbol, bids, asks);

//         // Redis-ə yazırıq. TTL çox qısa (2 saniyə) qoyuruq, çünki onsuz da hər 100ms-dən bir yenisi gəlir.
//         String redisKey = RedisKeys.LIVE_DEPTH.getKeyName() + ":" + symbol;
//         redisService.setValueWithExpire(redisKey, response, 10, TimeUnit.SECONDS);

         // Məlumatı həm 2 saniyəlik anbara yazır, həm də canlı (Pub/Sub) kanala atır
         redisService.processLiveStreamData(RedisKeys.LIVE_DEPTH, symbol, response);

         // Log.debug qoyuruq ki, saniyədə 10 dəfə konsolu dondurmasın (Test vaxtı info edə bilərsən)
         log.debug("DEPTH Updated for {}: {} Bids, {} Asks", symbol, bids.size(), asks.size());

      } catch (Exception e) {
         log.error("Error parsing Binance DEPTH WSS message", e);
      }
   }

   private List<DepthEntry> parseDepthArray(JsonNode node, String key1, String key2) {
      List<DepthEntry> list = new ArrayList<>();
      JsonNode arrayNode = node.has(key1) ? node.get(key1) : node.get(key2);

      if (arrayNode != null && arrayNode.isArray()) {
         for (JsonNode entry : arrayNode) {
            list.add(new DepthEntry(entry.get(0).asText(), entry.get(1).asText()));
         }
      }
      return list;
   }

   @Override
   public void afterConnectionEstablished(WebSocketSession session) {
      log.info("Binance DEPTH WebSocket connected! Session ID: {}", session.getId());
   }

   @Override
   public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
      log.warn("Binance DEPTH WebSocket disconnected! Status: {}", status);
   }
}