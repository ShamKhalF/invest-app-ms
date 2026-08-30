package az.shlf.marketdataservice.scheduler;

import az.shlf.marketdataservice.cache.RedisService;
import az.shlf.marketdataservice.client.BinanceExchangeInfoClient;
import az.shlf.marketdataservice.entity.SymbolHourlyKlineEntity;
import az.shlf.marketdataservice.entity.TopWatchedSymbolEntity;
import az.shlf.marketdataservice.repository.SymbolHourlyKlineRepository;
import az.shlf.marketdataservice.repository.TopWatchedSymbolRepository;
import az.shlf.marketdataservice.service.MarketDataKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketDataScheduler {

   private final RedisService redisService;
   private final TopWatchedSymbolRepository topWatchedSymbolRepository;
   private final SymbolHourlyKlineRepository klineRepository;
   private final BinanceExchangeInfoClient exchangeInfoClient;
   private final MarketDataKafkaProducer kafkaProducer;

   private static final String TOP_SYMBOLS_ZSET_KEY = "top_watched_symbols_zset";

   @Scheduled(fixedRate = 300000)
   @Transactional
   public void syncTopSymbolsToDb() {
      Set<ZSetOperations.TypedTuple<Object>> symbolsWithScores = redisService.getZSetWithScores(TOP_SYMBOLS_ZSET_KEY);

      if (symbolsWithScores != null && !symbolsWithScores.isEmpty()) {
         List<String> symbolsInRedis = symbolsWithScores.stream()
                 .filter(tuple -> tuple.getValue() != null)
                 .map(tuple -> tuple.getValue().toString())
                 .collect(Collectors.toList());

         Map<String, TopWatchedSymbolEntity> existingEntities = topWatchedSymbolRepository.findAllById(symbolsInRedis)
                 .stream()
                 .collect(Collectors.toMap(TopWatchedSymbolEntity::getSymbol, entity -> entity));

         List<TopWatchedSymbolEntity> entitiesToUpdate = new ArrayList<>();

         for (ZSetOperations.TypedTuple<Object> tuple : symbolsWithScores) {
            if (tuple.getValue() == null) continue;

            String symbol = tuple.getValue().toString();
            Long scoreToAdd = tuple.getScore() != null ? tuple.getScore().longValue() : 0L;

            if (existingEntities.containsKey(symbol)) {
               TopWatchedSymbolEntity entity = existingEntities.get(symbol);
               entity.setWatchCount(entity.getWatchCount() + scoreToAdd);
               entitiesToUpdate.add(entity);
            } else {
               entitiesToUpdate.add(TopWatchedSymbolEntity.builder()
                       .symbol(symbol)
                       .name(symbol)
                       .watchCount(scoreToAdd)
                       .build());
            }
         }

         topWatchedSymbolRepository.saveAll(entitiesToUpdate);
         redisService.deleteValue(TOP_SYMBOLS_ZSET_KEY);
         log.info("Synced watch counts from Redis to Database and cleared Redis ZSET.");
      }
   }

   @Scheduled(cron = "0 1 * * * *")
   public void fetchAndSaveHourlyKlinesForTopSymbols() {
      List<TopWatchedSymbolEntity> top10Symbols = topWatchedSymbolRepository.findTop20ByOrderByWatchCountDesc();

      if (top10Symbols == null || top10Symbols.isEmpty()) {
         log.warn("No symbols found in the database to fetch hourly klines.");
         return;
      }

      List<SymbolHourlyKlineEntity> entitiesToSave = new ArrayList<>();

      for (TopWatchedSymbolEntity entity : top10Symbols) {
         String symbol = entity.getSymbol();
         try {
            Long lastDbCloseTime = klineRepository.findMaxCloseTimeBySymbol(symbol);
            if (lastDbCloseTime == null) {
               lastDbCloseTime = 0L;
            }

            List<Object[]> klines = exchangeInfoClient.getKlines(symbol, "1h", 2);

            if (klines != null && klines.size() >= 2) {
               Object[] lastClosedKline = klines.get(0);
               long currentCloseTime = ((Number) lastClosedKline[6]).longValue();
               long currentTime = System.currentTimeMillis();

               if (currentCloseTime > lastDbCloseTime && currentCloseTime < currentTime) {
                  SymbolHourlyKlineEntity build = SymbolHourlyKlineEntity.builder()
                          .symbol(symbol)
                          .openTime(((Number) lastClosedKline[0]).longValue())
                          .openPrice(new BigDecimal(lastClosedKline[1].toString()))
                          .highPrice(new BigDecimal(lastClosedKline[2].toString()))
                          .lowPrice(new BigDecimal(lastClosedKline[3].toString()))
                          .closePrice(new BigDecimal(lastClosedKline[4].toString()))
                          .volume(new BigDecimal(lastClosedKline[5].toString()))
                          .closeTime(currentCloseTime)
                          .build();

                  entitiesToSave.add(build);

                  kafkaProducer.sendKlineData(build);
               }
            }
         } catch (Exception e) {
            log.error("Error fetching hourly Kline data for top symbol {}: {}", symbol, e.getMessage());
         }

      }

      if (!entitiesToSave.isEmpty()) {
         klineRepository.saveAll(entitiesToSave);
         log.info("Successfully saved hourly klines for Top 10 watched symbols.");
      }
   }
}