package az.shlf.marketdataservice.client;

import az.shlf.marketdataservice.model.symbol.ExchangeInfoResponse;
import az.shlf.marketdataservice.model.ticker.Ticker24hResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "binanceExchangeInfoClient", url = "${binance.api.base-url}")
public interface BinanceExchangeInfoClient {

    @GetMapping("/api/v3/exchangeInfo")
    ExchangeInfoResponse getExchangeInfo();

//    https://testnet.binance.vision/api/v3/ticker/24hr?symbol=BTCUSDT
   @GetMapping("/api/v3/ticker/24hr")
   Ticker24hResponse get24hTicker(@RequestParam("symbol") String symbol);

//   https://testnet.binance.vision/api/v3/ticker/24hr?symbols=["BTCUSDT","ETHUSDT","BNBUSDT"]
//   https://testnet.binance.vision/api/v3/ticker/24hr?symbols=%5B%22BTCUSDT%22,%22ETHUSDT%22,%22BNBUSDT%22%5D
   @GetMapping("/api/v3/ticker/24hr")
   List<Ticker24hResponse> get24hTickers(@RequestParam("symbols") String symbols);

//   https://testnet.binance.vision/api/v3/ticker/24hr
   @GetMapping("/api/v3/ticker/24hr")
   List<Ticker24hResponse> getAll24hTickers();

//   https://testnet.binance.vision/api/v3/klines?symbol=BTCUSDT&interval=1w&limit=52
   @GetMapping("/api/v3/klines")
   List<Object[]> getKlines(
           @RequestParam("symbol") String symbol,
           @RequestParam("interval") String interval,
           @RequestParam("limit") int limit
   );

//   https://testnet.binance.vision/api/v3/klines?symbol=BTCUSDT&interval=1d&startTime=1672531200000&endTime=1675123200000
   @GetMapping("/api/v3/klines")
   List<Object[]> getHistoricalKlinesByRange(
           @RequestParam("symbol") String symbol,
           @RequestParam("interval") String interval,
           @RequestParam("startTime") Long startTime,
           @RequestParam("endTime") Long endTime,
           @RequestParam("limit") int limit
   );

}
