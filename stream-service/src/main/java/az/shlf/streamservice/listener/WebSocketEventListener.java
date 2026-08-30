package az.shlf.streamservice.listener;

import az.shlf.streamservice.cache.RedisSessionService;
import az.shlf.streamservice.publisher.SymbolWatchPublisher;
import az.shlf.streamservice.service.MarketCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

   private final RedisSessionService redisSessionService;
   private final MarketCommandService marketCommandService;
   private final SymbolWatchPublisher symbolWatchPublisher;


   @EventListener
   public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
      StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
      String sessionId = headerAccessor.getSessionId();
      String destination = headerAccessor.getDestination();

      if (destination != null && destination.startsWith("/topic/")) {
         redisSessionService.addUserToRoom(destination, sessionId);
         log.info("✅ Yeni abunə! Session: {}, Otaq: {}", sessionId, destination);

         String[] parts = destination.split("/");
         if (parts.length >= 4) {
            String streamType = parts[2];
            String symbol = parts[3];

            // 1. Koin reytinqini artırmaq üçün Kafka-ya siqnal göndəririk
            symbolWatchPublisher.publishSymbol(symbol);

            // 2. Otaqdakı ilk istifadəçidirsə Binance-dan axını başladırıq
            Long currentUsers = redisSessionService.getRoomUserCount(destination);
            if (currentUsers != null && currentUsers == 1L) {
               marketCommandService.updateStreamState(streamType, symbol, true);
            }
         }
      }
   }


   @EventListener
   public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
      StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
      String sessionId = headerAccessor.getSessionId();

      Set<Object> userRooms = redisSessionService.getUserRooms(sessionId);

      if (userRooms != null && !userRooms.isEmpty()) {
         for (Object roomObj : userRooms) {
            String room = roomObj.toString();
            redisSessionService.removeUserFromRoom(room, sessionId);

            Long remainingUsers = redisSessionService.getRoomUserCount(room);

            if (remainingUsers == null || remainingUsers == 0L) {
               log.info("🚨 Otaq tamamilə boşaldı! {}", room);

               String[] parts = room.split("/");
               if (parts.length >= 4) {
                  marketCommandService.updateStreamState(parts[2], parts[3], false);
               }
            }
         }
      }

      redisSessionService.clearUser(sessionId);
   }
}