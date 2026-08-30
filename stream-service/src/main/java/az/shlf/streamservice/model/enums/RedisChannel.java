package az.shlf.streamservice.model.enums;

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

   /**
    * Axın növünə uyğun Redis Kanalını dinamik təyin edir.
    */
   public static RedisChannel fromStreamType(StreamType streamType) {
      return switch (streamType) {
         case TICKER -> TICKER_CHANNEL;
         case DEPTH  -> DEPTH_CHANNEL;
         case KLINE  -> KLINE_CHANNEL;
      };
   }
}