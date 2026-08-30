package az.shlf.marketdataservice.model.wss;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveTickerResponse {

   // Koinin adı (Məsələn: "BTCUSDT") -> Binance JSON-da "s" olaraq gəlir
   private String symbol;

   // O anki canlı qiymət (Məsələn: "81500.50") -> Binance JSON-da "c" olaraq gəlir
   private String currentPrice;

   // Son 24 saatdakı faiz dəyişimi (Məsələn: "-1.5" və ya "2.3") -> Binance JSON-da "P" olaraq gəlir
   private String priceChangePercent;

}