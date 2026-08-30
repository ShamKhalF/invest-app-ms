package az.shlf.streamservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Bu annotasiya arxa planda STOMP Poçt İdarəsini işə salır
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

   @Override
   public void registerStompEndpoints(StompEndpointRegistry registry) {
      // 1. FRONTEND-İN QOŞULACAĞI ANA QAPI (Handshake Endpoint)
      // Frontend tərəfi WSS əlaqəsini məhz "ws://sənin-domain.com/ws-stream" ünvanından başladacaq
      registry.addEndpoint("/ws-stream")
              .setAllowedOriginPatterns("*") // CORS - Test üçün hamıya açıq qoyuruq (Prod-da yalnız Front domaini olacaq)
              .withSockJS(); // Əgər köhnə brauzer WSS dəstəkləmirsə, Fallback (ehtiyat) kimi HTTP üzərindən simulyasiya edir
   }

   @Override
   public void configureMessageBroker(MessageBrokerRegistry registry) {
      // 2. OTAQLARIN ADLARI (Broadcast)
      // Serverdən (Stream MS) çıxan və Frontend-ə gedən bütün dataların ünvanı "/topic" ilə başlayacaq.
      // Məsələn: /topic/depth/btcusdt
      registry.enableSimpleBroker("/topic");

      // 3. FRONTEND-DƏN GƏLƏN MESAJLARIN ADLARI (İstəyə bağlı)
      // Əgər Frontend-dən kimsə serverə nəsə göndərmək istəsə (məsələn, chat mesajı), o "/app" ilə başlamalıdır.
      // Bizim halda Frontend bizə data göndərməyəcək, yalnız dinləyəcək. Amma standart olaraq bu da yazılır.
      registry.setApplicationDestinationPrefixes("/app");
   }

}
