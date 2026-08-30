package az.shlf.marketdataservice.service;

import az.shlf.marketdataservice.model.enums.ChartTimeFrame;
import az.shlf.marketdataservice.model.enums.KlineInterval;
import az.shlf.marketdataservice.model.kline.KlineResponse;

import java.util.List;

public interface BinanceKlineService {
   List<KlineResponse> getHistoricalKlines(String symbol, ChartTimeFrame timeFrame, KlineInterval interval);
   List<KlineResponse> getKlinesByDateRange(String symbol, KlineInterval interval, Long startTime, Long endTime);
}
