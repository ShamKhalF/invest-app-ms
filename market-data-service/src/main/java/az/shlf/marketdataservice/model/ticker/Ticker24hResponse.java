package az.shlf.marketdataservice.model.ticker;

import lombok.Data;

@Data
public class Ticker24hResponse {
   private String symbol;
   private String priceChange;
   private String priceChangePercent;
   private String lastPrice;
   private String highPrice;
   private String lowPrice;
   private String volume;
   private String quoteVolume;

}