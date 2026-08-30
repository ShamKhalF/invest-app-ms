package az.shlf.marketdataservice.model.enums;

import lombok.Getter;

@Getter
public enum RedisKeys {

//   SYMBOL_METADATA("ALL_SYMBOL_METADATA:"),
   TICKER_24H("TICKER_24H:"),
   KLINES("KLINES:"),
   KLINES_RANGE("KLINES_RANGE:"),
   STREAM_STATUS_KEY("ADMIN:MARKET_DATA_STREAM:STATUS"),

   // Canlı WSS axınları üçün həm Anbar Açarı, həm də Rasiya Kanalı təyin edirik
   LIVE_TICKER("LIVE_TICKER:", "ticker-events-channel"),
   LIVE_KLINE("LIVE_KLINE:", "kline-events-channel"),
   LIVE_DEPTH("LIVE_DEPTH:", "depth-events-channel");

   private final String keyName;
   private final String channelName;

   // Kanala ehtiyacı olmayan adi məlumatlar üçün konstruktor
   RedisKeys(String keyName) {
      this.keyName = keyName;
      this.channelName = null;
   }

   // Həm yaddaşa yazılıb, həm də canlı (Pub/Sub) yayımlanacaq məlumatlar üçün konstruktor
   RedisKeys(String keyName, String channelName) {
      this.keyName = keyName;
      this.channelName = channelName;
   }


}
