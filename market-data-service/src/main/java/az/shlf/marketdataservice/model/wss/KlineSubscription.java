package az.shlf.marketdataservice.model.wss;

import az.shlf.marketdataservice.model.enums.KlineInterval;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KlineSubscription {

   private String symbol;         // Məs: BTCUSDT
   private KlineInterval interval; // ARTIQ ENUM-DUR!

   // URL üçün stream adını düzəldirik
   public String getStreamName() {
      // Interval artıq Enum olduğu üçün birbaşa getValue() edirik
      return symbol.toLowerCase() + "@kline_" + interval.getValue();
   }

}
