package az.shlf.marketdataservice.controller;

import az.shlf.marketdataservice.model.enums.ChartTimeFrame;
import az.shlf.marketdataservice.model.enums.KlineInterval;
import az.shlf.marketdataservice.model.kline.KlineResponse;
import az.shlf.marketdataservice.model.ticker.Ticker24hResponse;
import az.shlf.marketdataservice.service.BinanceKlineService;
import az.shlf.marketdataservice.service.BinanceTickerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/market")
@RequiredArgsConstructor
public class MarketDataController {

   private final BinanceTickerService tickerService;
   private final BinanceKlineService klineService;

   @GetMapping("/ticker/24hr")
   public ResponseEntity<Ticker24hResponse> get24hTicker(@RequestParam String symbol) {
      return ResponseEntity.ok(tickerService.get24hTicker(symbol.toUpperCase()));
   }

   @GetMapping("/tickers/24hr")
   public ResponseEntity<List<Ticker24hResponse>> get24hTickers(@RequestParam List<String> symbols) {
      List<String> upperCaseSymbols = symbols.stream()
              .map(String::toUpperCase)
              .collect(Collectors.toList());

      return ResponseEntity.ok(tickerService.get24hTickers(upperCaseSymbols));
   }


   @GetMapping("/klines")
   public ResponseEntity<List<KlineResponse>> getHistoricalKlines(
           @RequestParam String symbol,
           @RequestParam ChartTimeFrame timeFrame,
           @RequestParam KlineInterval interval) {

      return ResponseEntity.ok(klineService.getHistoricalKlines(symbol.toUpperCase(), timeFrame, interval));
   }

   @GetMapping("/klines/range")
   public ResponseEntity<List<KlineResponse>> getKlinesByDateRange(
           @RequestParam String symbol,
           @RequestParam KlineInterval interval,
           @RequestParam Long startTime,
           @RequestParam Long endTime) {

      return ResponseEntity.ok(klineService.getKlinesByDateRange(symbol.toUpperCase(), interval, startTime, endTime));
   }

}