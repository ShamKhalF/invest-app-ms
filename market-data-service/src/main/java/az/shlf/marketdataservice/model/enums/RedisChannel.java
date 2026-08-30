package az.shlf.marketdataservice.model.enums;

import lombok.Getter;

@Getter
public enum RedisChannel {
   TICKER_CHANNEL("tracked-symbols-ticker"),
   DEPTH_CHANNEL("tracked-symbols-depth"),
   KLINE_CHANNEL("tracked-symbols-kline");

   private final String channelName;

   RedisChannel(String channelName) {
      this.channelName = channelName;
   }
}