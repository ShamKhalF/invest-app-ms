package az.shlf.marketdataservice.service;

import az.shlf.marketdataservice.model.ticker.Ticker24hResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface BinanceTickerService {
   Ticker24hResponse get24hTicker(String symbol);
   List<Ticker24hResponse> get24hTickers(List<String> symbols);
   List<Ticker24hResponse> getAll24hTickers(long timeout, TimeUnit unit);
}
