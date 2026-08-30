package az.shlf.marketdataservice.service;

import az.shlf.marketdataservice.client.BinanceExchangeInfoClient;
import az.shlf.marketdataservice.entity.SymbolHourlyKlineEntity;
import az.shlf.marketdataservice.entity.TopWatchedSymbolEntity;
import az.shlf.marketdataservice.model.symbol.ExchangeInfoResponse;
import az.shlf.marketdataservice.model.symbol.SymbolInfo;
import az.shlf.marketdataservice.repository.SymbolHourlyKlineRepository;
import az.shlf.marketdataservice.repository.TopWatchedSymbolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataStartupService {

   private final BinanceExchangeInfoClient exchangeInfoClient;
   private final TopWatchedSymbolRepository topWatchedSymbolRepository;
   private final SymbolHourlyKlineRepository symbolHourlyKlineRepository;
   private final MarketDataKafkaProducer kafkaProducer;

   private static final List<String> POPULAR_SYMBOLS = List.of(
           "BTCUSDT", "ETHUSDT", "BNBUSDT", "SOLUSDT", "XRPUSDT",
           "ADAUSDT", "DOGEUSDT", "AVAXUSDT", "DOTUSDT", "LINKUSDT",
           "MATICUSDT", "LTCUSDT", "TRXUSDT", "SHIBUSDT", "NEARUSDT",
           "ATOMUSDT", "UNIUSDT", "BCHUSDT", "ETCUSDT", "XLMUSDT"
   );

   @EventListener(ApplicationReadyEvent.class)
   public void initSymbolsOnStartup() {
      log.info("App started: Syncing symbols from Binance to database...");
      try {
         ExchangeInfoResponse response = exchangeInfoClient.getExchangeInfo();
         if (response == null || response.getSymbols() == null) {
            return;
         }

         Set<String> existingSymbols = topWatchedSymbolRepository.findAll()
                 .stream()
                 .map(TopWatchedSymbolEntity::getSymbol)
                 .collect(Collectors.toSet());

         List<TopWatchedSymbolEntity> newEntities = new ArrayList<>();
         List<String> symbolsToFetchHistory = new ArrayList<>();

         for (SymbolInfo symbolInfo : response.getSymbols()) {
            if ("TRADING".equalsIgnoreCase(symbolInfo.getStatus())) {
               String symbol = symbolInfo.getSymbol();

               // 1. Yeni koinləri əsas cədvələ əlavə edirik (1300+ koinin hamısı)
               if (!existingSymbols.contains(symbol)) {
                  String generatedName = symbolInfo.getBaseAsset() + "/" + symbolInfo.getQuoteAsset();
                  newEntities.add(TopWatchedSymbolEntity.builder()
                          .symbol(symbol)
                          .name(generatedName)
                          .watchCount(0L)
                          .build());
               }

               // 2. Keçmiş datası çəkiləcək koinləri yalnız məşhur siyahıdan seçirik
               if (POPULAR_SYMBOLS.contains(symbol.toUpperCase())) {
                  symbolsToFetchHistory.add(symbol);
               }
            }
         }

         if (!newEntities.isEmpty()) {
            topWatchedSymbolRepository.saveAll(newEntities);
            log.info("Successfully added {} new symbols to the database.", newEntities.size());
         }

         // Artıq bu metod 1300+ deyil, yalnız 20 məşhur koin üçün işləyəcək
         if (!symbolsToFetchHistory.isEmpty()) {
            fetchHistoricalDataAsync(symbolsToFetchHistory, 720);
         }

      } catch (Exception e) {
         log.error("Failed to sync symbols on startup", e);
      }
   }

   @Async
   public void fetchHistoricalDataAsync(List<String> symbols, int limit) {
      log.info("Starting background historical data fetch for {} symbols", symbols.size());

      for (String symbol : symbols) {
         try {
            Long lastDbCloseTime = symbolHourlyKlineRepository.findMaxCloseTimeBySymbol(symbol);
            if (lastDbCloseTime == null) {
               lastDbCloseTime = 0L;
            }

            List<Object[]> klines = exchangeInfoClient.getKlines(symbol, "1h", limit);

            if (klines != null && !klines.isEmpty()) {
               List<SymbolHourlyKlineEntity> klineEntities = new ArrayList<>();

               for (Object[] kline : klines) {
                  long currentCloseTime = ((Number) kline[6]).longValue();
                  long currentTime = System.currentTimeMillis();

// Yalnız bazadakı son vaxtdan yeni olan VƏ tam qapanmış (vaxtı keçmiş) şamları əlavə et
                  if (currentCloseTime > lastDbCloseTime && currentCloseTime < currentTime) {
                     SymbolHourlyKlineEntity build = SymbolHourlyKlineEntity.builder()
                             .symbol(symbol)
                             .openTime(((Number) kline[0]).longValue())
                             .openPrice(new BigDecimal(kline[1].toString()))
                             .highPrice(new BigDecimal(kline[2].toString()))
                             .lowPrice(new BigDecimal(kline[3].toString()))
                             .closePrice(new BigDecimal(kline[4].toString()))
                             .volume(new BigDecimal(kline[5].toString()))
                             .closeTime(currentCloseTime)
                             .build();

                     klineEntities.add(build);

                     kafkaProducer.sendKlineData(build);
                     
                  }
               }

               if (!klineEntities.isEmpty()) {
                  symbolHourlyKlineRepository.saveAll(klineEntities);
               }
            }

            Thread.sleep(150);

         } catch (Exception e) {
            log.error("Error fetching historical data for {}: {}", symbol, e.getMessage());
         }
      }
      log.info("Background historical data fetch completed.");
   }
}