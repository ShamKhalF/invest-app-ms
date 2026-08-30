package az.shlf.marketdataservice.websocket.depth;

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
public class BinanceDepthWssManager {

   private final BinanceDepthWebSocketHandler depthHandler;
   private final WssProperties wssProperties;

   private WebSocketSession currentSession;
   private Set<String> currentlySubscribedSymbols;

   public void updateWssStreams(Set<String> targetSymbols) {
      if (targetSymbols == null || targetSymbols.isEmpty()) {
         stopWss();
         return;
      }

      if (targetSymbols.equals(currentlySubscribedSymbols) && currentSession != null && currentSession.isOpen()) {
         return;
      }

      log.info("Updating Binance DEPTH WSS connection for: {}", targetSymbols);

      closeCurrentSession();
      connectToBinance(targetSymbols);
      currentlySubscribedSymbols = targetSymbols;
   }

   private void connectToBinance(Set<String> symbols) {
      String urlString;

      if (symbols.size() == 1) {
         String symbol = symbols.iterator().next().toLowerCase();
         urlString = wssProperties.getBaseUrl() + String.format(wssProperties.getDepthSinglePath(), symbol);
      } else {
         String streams = symbols.stream()
                 .map(sym -> sym.toLowerCase() + "@depth20@100ms") // Çoxlu koin üçün format
                 .collect(Collectors.joining("/"));
         urlString = wssProperties.getBaseUrl() + String.format(wssProperties.getDepthMultiPath(), streams);
      }

      try {
         StandardWebSocketClient client = new StandardWebSocketClient();
         currentSession = client.execute(depthHandler, urlString).get();
         log.info("Successfully initiated DEPTH WSS connection to: {}", urlString);
      } catch (Exception e) {
         log.error("Failed to connect to Binance DEPTH WSS", e);
      }
   }

   public void stopWss() {
      log.info("Manual DEPTH WSS stop requested!");
      closeCurrentSession();
      if (currentlySubscribedSymbols != null) {
         currentlySubscribedSymbols.clear();
      }
   }

   @PreDestroy
   public void onApplicationShutdown() {
      log.info("Shutting down... Closing DEPTH WSS securely.");
      closeCurrentSession();
   }

   private void closeCurrentSession() {
      if (currentSession != null && currentSession.isOpen()) {
         try {
            currentSession.close();
         } catch (Exception e) {
            log.error("Error closing DEPTH WSS session", e);
         }
      }
   }
}