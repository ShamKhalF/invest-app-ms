package az.shlf.orderservice.socket;

import az.shlf.orderservice.client.BinanceOrderClient;
import az.shlf.orderservice.dto.BinanceExecutionReport;
import az.shlf.orderservice.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Map;
import java.util.concurrent.CompletionStage;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinanceWebSocketService {

   private final BinanceOrderClient binanceOrderClient;
   private final OrderService orderService; // interface üzerinden çağırılır
   private final ObjectMapper objectMapper;

   @Value("${binance.api.key}")
   private String apiKey;

   @Value("${binance.wss.base-url:wss://testnet.binance.vision/ws/}")
   private String wssBaseUrl;

   private String currentListenKey;
   private WebSocket webSocket;

   @PostConstruct
   public void initConnection() {
      startUserDataStream();
   }

   private void startUserDataStream() {
      try {
         Map<String, String> response = binanceOrderClient.startUserDataStream(apiKey);
         this.currentListenKey = response.get("listenKey");
         connectWebSocket();
      } catch (Exception e) {
         log.error("ListenKey alınarkən xəta baş verdi: {}", e.getMessage());
      }
   }

   private void connectWebSocket() {
      if (this.currentListenKey == null) {
         return;
      }

      HttpClient client = HttpClient.newHttpClient();
      String wssUrl = wssBaseUrl + currentListenKey;

      client.newWebSocketBuilder()
              .buildAsync(URI.create(wssUrl), new WebSocket.Listener() {
                 StringBuilder payloadBuilder = new StringBuilder();

                 @Override
                 public void onOpen(WebSocket webSocket) {
                    log.info("Binance UserDataStream WebSocket bağlantısı uğurla quruldu.");
                    WebSocket.Listener.super.onOpen(webSocket);
                 }

                 @Override
                 public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                    payloadBuilder.append(data);
                    if (last) {
                       processMessage(payloadBuilder.toString());
                       payloadBuilder.setLength(0);
                    }
                    return WebSocket.Listener.super.onText(webSocket, data, last);
                 }

                 @Override
                 public void onError(WebSocket webSocket, Throwable error) {
                    log.error("WebSocket xətası: {}", error.getMessage());
                    reconnect();
                 }
              });
   }

   private void processMessage(String message) {
      try {
         BinanceExecutionReport report = objectMapper.readValue(message, BinanceExecutionReport.class);
         if ("executionReport".equals(report.getEventType())) {
            orderService.handleExecutionReport(report);
         }
      } catch (Exception e) {
         log.error("Gələn WebSocket mesajı oxunarkən xəta: {}", e.getMessage());
      }
   }

   // Hər 30 dəqiqədən bir listenKey-i canlı saxlamaq üçün
   @Scheduled(fixedRate = 1800000)
   public void keepAliveListenKey() {
      if (currentListenKey != null) {
         try {
            binanceOrderClient.keepAliveUserDataStream(apiKey, currentListenKey);
            log.info("ListenKey uğurla yeniləndi.");
         } catch (Exception e) {
            log.error("ListenKey yenilənməsi uğursuz oldu, yenidən bağlantı qurulur...");
            reconnect();
         }
      }
   }

   private void reconnect() {
      if (webSocket != null) {
         webSocket.abort();
      }
      startUserDataStream();
   }

}