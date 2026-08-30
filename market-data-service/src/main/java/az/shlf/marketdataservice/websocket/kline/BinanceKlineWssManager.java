package az.shlf.marketdataservice.websocket.kline;

import az.shlf.marketdataservice.config.properties.WssProperties;
import az.shlf.marketdataservice.model.wss.KlineSubscription;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinanceKlineWssManager {

   private final BinanceKlineWebSocketHandler klineHandler;
   private final WssProperties wssProperties;

   private WebSocketSession currentSession;
   private Set<KlineSubscription> currentlySubscribedKlines;


   public void updateWssStreams(Set<KlineSubscription> targetKlines) {

      if (targetKlines == null || targetKlines.isEmpty()) {
         stopWss();
         currentlySubscribedKlines = targetKlines;
         return;
      }

      if (targetKlines.equals(currentlySubscribedKlines) && currentSession != null && currentSession.isOpen()) {
         return;
      }

      log.info("Updating Binance KLINE WSS connection for new subscriptions: {}", targetKlines);

      closeCurrentSession();
      connectToBinance(targetKlines);
      currentlySubscribedKlines = targetKlines;
   }

   private void connectToBinance(Set<KlineSubscription> klines) {
      String urlString;

      if (klines.size() == 1) {
         KlineSubscription kline = klines.iterator().next();
         urlString = wssProperties.getBaseUrl() + String.format(wssProperties.getKlineSinglePath(), kline.getStreamName());
      } else {
         String streams = klines.stream()
                 .map(KlineSubscription::getStreamName)
                 .collect(Collectors.joining("/"));
         urlString = wssProperties.getBaseUrl() + String.format(wssProperties.getKlineMultiPath(), streams);
      }

      try {
         StandardWebSocketClient client = new StandardWebSocketClient();
         currentSession = client.execute(klineHandler, urlString).get();
         log.info("Successfully initiated KLINE WSS connection to: {}", urlString);

      } catch (Exception e) {
         log.error("Failed to connect to Binance KLINE WSS at URL: {}", urlString, e);
      }
   }

   private void closeCurrentSession() {
      if (currentSession != null && currentSession.isOpen()) {
         try {
            currentSession.close();
            log.info("Closed previous Binance KLINE WSS session.");
         } catch (Exception e) {
            log.error("Error closing KLINE WSS session", e);
         }
      }
   }

   public void stopWss() {
      log.info("Manual KLINE WSS stop requested!");
      closeCurrentSession();
      if (currentlySubscribedKlines != null) {
         currentlySubscribedKlines.clear();
      }
   }

   @PreDestroy
   public void onApplicationShutdown() {
      log.info("Shutting down... Closing KLINE WSS securely.");
      closeCurrentSession();
   }

}
