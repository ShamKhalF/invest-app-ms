package az.shlf.marketdataservice.controller;

import az.shlf.marketdataservice.dto.KlineSearchDto;
import az.shlf.marketdataservice.dto.TopSymbolSearchDto;
import az.shlf.marketdataservice.dto.response.SymbolHourlyKlineResponseDto;
import az.shlf.marketdataservice.dto.response.TopWatchedSymbolResponseDto;
import az.shlf.marketdataservice.service.MarketDataAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class MarketDataAnalyticsController {

   private final MarketDataAnalyticsService analyticsService;

   @GetMapping("/top-symbols")
   public ResponseEntity<Page<TopWatchedSymbolResponseDto>> getTopSymbols(@ModelAttribute TopSymbolSearchDto searchDto) {
      return ResponseEntity.ok(analyticsService.getTopSymbols(searchDto));
   }

   @GetMapping("/historical-prices")
   public ResponseEntity<Page<SymbolHourlyKlineResponseDto>> getHistoricalPrices(@ModelAttribute KlineSearchDto searchDto) {
      return ResponseEntity.ok(analyticsService.getKlinesWithFilters(searchDto));
   }
}