package az.shlf.marketdataservice.listener;

import az.shlf.marketdataservice.model.enums.KlineInterval;
import az.shlf.marketdataservice.model.enums.RedisChannel;
import az.shlf.marketdataservice.model.wss.KlineSubscription;
import az.shlf.marketdataservice.service.AdminStateService;
import az.shlf.marketdataservice.websocket.depth.BinanceDepthWssManager;
import az.shlf.marketdataservice.websocket.kline.BinanceKlineWssManager;
import az.shlf.marketdataservice.websocket.ticker.BinanceWssManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketDataRedisListener implements MessageListener {

   private final ObjectMapper objectMapper;
   private final BinanceWssManager tickerWssManager;
   private final BinanceDepthWssManager depthWssManager;
   private final BinanceKlineWssManager klineWssManager;
   private final AdminStateService adminStateService;

   @Override
   public void onMessage(Message message, byte[] pattern) {
      try {
         // 1. KİLİDİ YOXLA! Əgər admin sistemi söndürübsə, heç nə etmə.
         if (!adminStateService.isStreamActive()) {
            log.info("🛑 Sistem admin tərəfindən dondurulub. Yeni WSS açılışı ignor edildi.");
            return;
         }

         String channel = new String(message.getChannel());
         String body = new String(message.getBody());

         log.info("📥 [REDIS PUB/SUB] Mesaj gəldi | Kanal: {} | Data: {}", channel, body);

         // JSON massivini (məs: ["BTCUSDT", "ETHUSDT"]) Set<String> obyektinə çeviririk
         Set<String> symbols = objectMapper.readValue(body, new TypeReference<Set<String>>() {});
         Set<String> formattedSymbols = symbols.stream()
                 .map(String::toLowerCase)
                 .collect(Collectors.toSet());

         // Kanal adına görə uyğun manager-i çağırırıq
         if (channel.equals(RedisChannel.TICKER_CHANNEL.getChannelName())) {
            tickerWssManager.updateWssStreams(formattedSymbols);

         } else if (channel.equals(RedisChannel.DEPTH_CHANNEL.getChannelName())) {
            depthWssManager.updateWssStreams(formattedSymbols);

         } else if (channel.equals(RedisChannel.KLINE_CHANNEL.getChannelName())) {

            // UYĞUNSUZLUĞUN HƏLLİ: Burada String əvəzinə Enum (KlineInterval) istifadə edirik.
            // Bütün gələn koinlər üçün default olaraq ONE_MINUTE (1 dəqiqə) şamını dinləyirik.
            Set<KlineSubscription> klineSubscriptions = formattedSymbols.stream()
                    .map(sym -> new KlineSubscription(sym, KlineInterval.ONE_MINUTE))
                    .collect(Collectors.toSet());

            klineWssManager.updateWssStreams(klineSubscriptions);
         }

      } catch (Exception e) {
         log.error("❌ Redis-dən gələn mesaj emal edilərkən xəta baş verdi!", e);
      }
   }

}
