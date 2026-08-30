package az.shlf.marketdataservice.service.impl;

import az.shlf.marketdataservice.cache.RedisService;
import az.shlf.marketdataservice.client.BinanceExchangeInfoClient;
import az.shlf.marketdataservice.model.enums.RedisKeys;
import az.shlf.marketdataservice.model.ticker.Ticker24hResponse;
import az.shlf.marketdataservice.service.BinanceTickerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinanceTickerServiceImpl implements BinanceTickerService {

   private final RedisService redisService;
   private final BinanceExchangeInfoClient binanceMarketClient;

   @Override
   public Ticker24hResponse get24hTicker(String symbol) {
      String redisKey = RedisKeys.TICKER_24H.getKeyName() + symbol;

      Ticker24hResponse cachedResponse = (Ticker24hResponse) redisService.getValue(redisKey);
      if (cachedResponse != null) {
         log.debug("Returning 24h ticker for {} from Redis Cache", symbol);
         return cachedResponse;
      }

      log.info("Fetching 24h ticker for {} from Binance API", symbol);
      Ticker24hResponse response = binanceMarketClient.get24hTicker(symbol);

      if (response != null) {
         redisService.setValueWithExpire(redisKey, response, 5, TimeUnit.SECONDS);
      }

      return response;
   }


   @Override
   public List<Ticker24hResponse> get24hTickers(List<String> symbols) {
      List<Ticker24hResponse> resultList = new ArrayList<>();
      List<String> missingSymbols = new ArrayList<>();

      List<String> redisKeys = symbols.stream()
              .map(sym -> RedisKeys.TICKER_24H.getKeyName() + sym)
              .collect(Collectors.toList());

      List<Object> cachedValues = redisService.multiGetValues(redisKeys);

      for (int i = 0; i < symbols.size(); i++) {
         Object cachedObj = cachedValues.get(i);
         if (cachedObj != null) {
            resultList.add((Ticker24hResponse) cachedObj);
         } else {
            missingSymbols.add(symbols.get(i));
         }
      }

      if (missingSymbols.isEmpty()) {
         log.debug("All {} symbols loaded from Redis Cache", symbols.size());
         return resultList;
      }

      log.info("Missing symbols in Redis: {}. Fetching from Binance...", missingSymbols.size());

      if (missingSymbols.size() == 1) {
         Ticker24hResponse singleResponse = get24hTicker(missingSymbols.getFirst()); // Bu metod özü Redis-ə yazacaq
         if (singleResponse != null) {
            resultList.add(singleResponse);
         }
         return resultList;
      }

      try {
         String symbolsParam = "[\"" + String.join("\",\"", missingSymbols) + "\"]";

         List<Ticker24hResponse> binanceResponses = binanceMarketClient.get24hTickers(symbolsParam);

         if (binanceResponses != null) {
            for (Ticker24hResponse response : binanceResponses) {
               resultList.add(response);

               String key = RedisKeys.TICKER_24H.getKeyName() + response.getSymbol();
               redisService.setValueWithExpire(key, response, 5, TimeUnit.SECONDS);
            }

         }

      } catch (Exception e) {
         log.error("Failed to fetch multiple tickers from Binance API", e);
      }

      return resultList;
   }


   @Override
   public List<Ticker24hResponse> getAll24hTickers(long timeout, TimeUnit unit) {
      log.warn("Fetching ALL 24h tickers from Binance API. Caution: High API Weight (40)!");

      List<Ticker24hResponse> allTickers = binanceMarketClient.getAll24hTickers();

      if (allTickers != null && !allTickers.isEmpty()) {
         log.info("Successfully fetched {} tickers from Binance. Saving to Redis...", allTickers.size());

         for (Ticker24hResponse ticker : allTickers) {
            String key = RedisKeys.TICKER_24H.getKeyName() + ticker.getSymbol();

            // Gelecekde Scheduler ile isleye biler deye ttl oradan gonderilecek
            redisService.setValueWithExpire(key, ticker, timeout, unit);
         }

         log.info("All {} tickers have been successfully cached in Redis.", allTickers.size());
      }

      return allTickers;
   }


}