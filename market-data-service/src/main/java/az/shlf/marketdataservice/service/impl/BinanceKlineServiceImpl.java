package az.shlf.marketdataservice.service.impl;

import az.shlf.marketdataservice.cache.RedisService;
import az.shlf.marketdataservice.client.BinanceExchangeInfoClient;
import az.shlf.marketdataservice.model.enums.ChartTimeFrame;
import az.shlf.marketdataservice.model.enums.KlineInterval;
import az.shlf.marketdataservice.model.enums.RedisKeys;
import az.shlf.marketdataservice.model.kline.KlineResponse;
import az.shlf.marketdataservice.service.BinanceKlineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinanceKlineServiceImpl implements BinanceKlineService {

   private final BinanceExchangeInfoClient binanceMarketClient;
   private final RedisService redisService;

   @Override
   @SuppressWarnings("unchecked")
   public List<KlineResponse> getHistoricalKlines(String symbol, ChartTimeFrame timeFrame, KlineInterval interval) {

      int limit = timeFrame.calculateLimit(interval);

      String redisKey = String.format("%s:%s:%s:%d",
              RedisKeys.KLINES.getKeyName(), symbol, interval.getValue(), limit);

      List<KlineResponse> cachedKlines = (List<KlineResponse>) redisService.getValue(redisKey);
      if (cachedKlines != null) {
         log.debug("Returning klines for {} (interval: {}, limit: {}) from Redis Cache", symbol, interval.getValue(), limit);
         return cachedKlines;
      }

      log.info("Fetching klines for {} from Binance API. Limit: {}", symbol, limit);

      List<Object[]> rawKlines = binanceMarketClient.getKlines(symbol, interval.getValue(), limit);

      if (rawKlines == null || rawKlines.isEmpty()) {
         return List.of();
      }

      List<KlineResponse> responseList = rawKlines.stream()
              .map(this::mapToKlineResponse)
              .collect(Collectors.toList());

      // 6. Dinamik TTL Vaxtını hesablayıb Redis-ə yazırıq
      long ttlSeconds = calculateDynamicTtl(interval);
      redisService.setValueWithExpire(redisKey, responseList, ttlSeconds, TimeUnit.SECONDS);

      return responseList;
   }

   @Override
   @SuppressWarnings("unchecked")
   public List<KlineResponse> getKlinesByDateRange(String symbol, KlineInterval interval, Long startTime, Long endTime) {

      // Binance eyni anda maksimum 1000 şam verə bilər. Keçmişi çəkəndə limit həmişə 1000 olaraq göndərilir
      // ki, o aralıqdakı datanı tam ala bilək.
      int limit = 1000;

      // Unikal Açar: KLINES_RANGE:BTCUSDT:1d:1672531200000:1675123200000
      String redisKey = String.format("%s:%s:%s:%d:%d",
              RedisKeys.KLINES_RANGE.getKeyName(), symbol, interval.getValue(), startTime, endTime);

      List<KlineResponse> cachedKlines = (List<KlineResponse>) redisService.getValue(redisKey);
      if (cachedKlines != null) {
         log.debug("Returning ranged klines for {} from Redis Cache", symbol);
         return cachedKlines;
      }

      log.info("Fetching ranged klines for {} from Binance API. Start: {}, End: {}", symbol, startTime, endTime);

      List<Object[]> rawKlines = binanceMarketClient.getHistoricalKlinesByRange(symbol, interval.getValue(), startTime, endTime, limit);

      if (rawKlines == null || rawKlines.isEmpty()) {
         return List.of();
      }

      List<KlineResponse> responseList = rawKlines.stream()
              .map(this::mapToKlineResponse)
              .collect(Collectors.toList());

      redisService.setValueWithExpire(redisKey, responseList, 1, TimeUnit.HOURS);

      return responseList;
   }


   /**
    * Binance-dən gələn qarışıq 12 elementli Array strukturunu DTO-ya çevirir.
    */
   private KlineResponse mapToKlineResponse(Object[] array) {
      KlineResponse response = new KlineResponse();

      response.setOpenTime(((Number) array[0]).longValue());
      response.setOpenPrice((String) array[1]);
      response.setHighPrice((String) array[2]);
      response.setLowPrice((String) array[3]);
      response.setClosePrice((String) array[4]);
      response.setVolume((String) array[5]);
      response.setCloseTime(((Number) array[6]).longValue());
      response.setQuoteAssetVolume((String) array[7]);
      response.setNumberOfTrades(((Number) array[8]).intValue());
      response.setTakerBuyBaseVolume((String) array[9]);
      response.setTakerBuyQuoteVolume((String) array[10]);
      // array[11] = "0" (İqnor edilir)

      return response;
   }

   /**
    * Şamın (Kline) intervalına uyğun olaraq Cache üçün dinamik yaşama müddəti (TTL) təyin edir.
    */
   private long calculateDynamicTtl(KlineInterval interval) {
      long duration = interval.getDurationInSeconds();

      if (duration <= 300) {
         // 5 dəqiqə və daha qısa şamlar (1m, 3m, 5m) üçün 15 saniyə Cache
         return 15L;
      } else if (duration <= 3600) {
         // 1 saat və daha qısa şamlar (15m, 30m, 1h) üçün 60 saniyə (1 dəqiqə) Cache
         return 60L;
      } else if (duration <= 86400) {
         // 1 gün və daha qısa şamlar (2h, 4h, 12h, 1d) üçün 5 dəqiqə Cache
         return 300L;
      } else {
         // Həftəlik və Aylıq şamlar üçün 15 dəqiqə Cache
         return 900L;
      }
   }

}
