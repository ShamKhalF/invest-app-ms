package az.shlf.marketdataservice.websocket.ticker;

import az.shlf.marketdataservice.config.properties.WssProperties;
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
public class BinanceWssManager {

   private final BinanceTickerWebSocketHandler tickerHandler;
   private final WssProperties wssProperties;

   private WebSocketSession currentSession;
   private Set<String> currentlySubscribedSymbols;

   public void updateWssStreams(Set<String> targetSymbols) {

      if (targetSymbols.isEmpty()) {
         stopWss();
         currentlySubscribedSymbols = targetSymbols;
         return;
      }

      if (targetSymbols.equals(currentlySubscribedSymbols) && currentSession != null && currentSession.isOpen()) {
         return;
      }

      log.info("Updating Binance WSS connection for new symbols: {}", targetSymbols);

      closeCurrentSession();
      connectToBinance(targetSymbols);
      currentlySubscribedSymbols = targetSymbols;
   }

   private void connectToBinance(Set<String> symbols) {
      String urlString;

      if (symbols.size() == 1) {
         String symbol = symbols.iterator().next();
         urlString = wssProperties.getBaseUrl() + String.format(wssProperties.getSinglePath(), symbol);
      } else {
         String streams = symbols.stream()
                 .map(sym -> sym + "@ticker")
                 .collect(Collectors.joining("/"));

         urlString = wssProperties.getBaseUrl() + String.format(wssProperties.getMultiPath(), streams);
      }

      try {
         StandardWebSocketClient client = new StandardWebSocketClient();
         currentSession = client.execute(tickerHandler, urlString).get();
         log.info("Successfully initiated WSS connection to: {}", urlString);

      } catch (Exception e) {
         log.error("Failed to connect to Binance WSS at URL: {}", urlString, e);
      }
   }

   private void closeCurrentSession() {
      if (currentSession != null && currentSession.isOpen()) {
         try {
            currentSession.close();
            log.info("Closed previous Binance WSS session.");
         } catch (Exception e) {
            log.error("Error closing WSS session", e);
         }
      }
   }

   public void stopWss() {
      log.info("Manual WSS stop requested!");
      closeCurrentSession();
      if (currentlySubscribedSymbols != null) {
         currentlySubscribedSymbols.clear();
      }
   }

   @PreDestroy
   public void onApplicationShutdown() {
      log.info("Application is shutting down. Closing Binance WSS securely to prevent Redis errors...");
      closeCurrentSession();
   }

}
