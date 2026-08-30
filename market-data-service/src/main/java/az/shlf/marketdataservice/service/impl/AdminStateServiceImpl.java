package az.shlf.marketdataservice.service.impl;

import az.shlf.marketdataservice.cache.RedisService;
import az.shlf.marketdataservice.model.enums.KlineInterval;
import az.shlf.marketdataservice.model.enums.RedisChannel;
import az.shlf.marketdataservice.model.enums.RedisKeys;
import az.shlf.marketdataservice.model.wss.KlineSubscription;
import az.shlf.marketdataservice.service.AdminStateService;
import az.shlf.marketdataservice.websocket.depth.BinanceDepthWssManager;
import az.shlf.marketdataservice.websocket.kline.BinanceKlineWssManager;
import az.shlf.marketdataservice.websocket.ticker.BinanceWssManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminStateServiceImpl implements AdminStateService {

   private final RedisService redisService;
   private final BinanceWssManager tickerWssManager;
   private final BinanceDepthWssManager depthWssManager;
   private final BinanceKlineWssManager klineWssManager;


   @Override
   public void setStreamActive(boolean isActive) {
      redisService.setValue(RedisKeys.STREAM_STATUS_KEY.getKeyName(), String.valueOf(isActive));
   }

   @Override
   public boolean isStreamActive() {
      Object statusObj = redisService.getValue(RedisKeys.STREAM_STATUS_KEY.getKeyName());
      if (statusObj == null) {
         return true; // Default olaraq TRUE qəbul edirik
      }
      // Obyekti String-ə çevirib təmizləyirik (əgər Jackson ətrafına dırnaq atıbsa silinməsi üçün)
      String statusStr = statusObj.toString().replace("\"", "");
      return Boolean.parseBoolean(statusStr);
   }

   @Override
   public Set<String> getActiveSymbols(RedisChannel channel) {
      String streamType = channel.name().replace("_CHANNEL", "");
      String key = "ACTIVE_SYMBOLS:" + streamType;

      Set<Object> members = redisService.getSetMembers(key);
      if (members == null || members.isEmpty()) {
         return new HashSet<>();
      }

      // RedisService Set<Object> qaytardığı üçün onu Set<String>-ə çeviririk
      return members.stream()
              .map(Object::toString)
              .collect(Collectors.toSet());
   }

   @Override
   public void stopAllStreams() {
      log.warn("🚨 ADMIN tərəfindən bütün canlı yayım SÖNDÜRÜLDÜ!");

      // 1. Statusu OFF edirik
      setStreamActive(false);

      // 2. Binance ilə olan bütün əlaqələri kəsirik
      tickerWssManager.stopWss();
      depthWssManager.stopWss();
      klineWssManager.stopWss();
   }

   @Override
   public void startAllStreams() {
      log.info("✅ ADMIN tərəfindən bütün canlı yayım YANDIRILDI!");

      // 1. Statusu ON edirik
      setStreamActive(true);

      // 2. Sönük olduğu müddətdə daxil olub gözləyən user-lərin simvollarını Redis-dən alırıq
      Set<String> tickerSymbols = getActiveSymbols(RedisChannel.TICKER_CHANNEL);
      Set<String> depthSymbols = getActiveSymbols(RedisChannel.DEPTH_CHANNEL);
      Set<String> klineSymbols = getActiveSymbols(RedisChannel.KLINE_CHANNEL);

      // 3. Əgər gözləyən koinlər varsa dərhal WSS-ləri işə salırıq
      if (!tickerSymbols.isEmpty()) {
         tickerWssManager.updateWssStreams(tickerSymbols);
      }
      if (!depthSymbols.isEmpty()) {
         depthWssManager.updateWssStreams(depthSymbols);
      }
      if (!klineSymbols.isEmpty()) {
         Set<KlineSubscription> klines = klineSymbols.stream()
                 .map(sym -> new KlineSubscription(sym, KlineInterval.ONE_MINUTE))
                 .collect(Collectors.toSet());
         klineWssManager.updateWssStreams(klines);
      }
   }

}
