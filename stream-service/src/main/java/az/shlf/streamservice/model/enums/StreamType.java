package az.shlf.streamservice.model.enums;

import lombok.Getter;

@Getter
public enum StreamType {

   TICKER("ticker-events-channel", "ticker"),
   KLINE("kline-events-channel", "kline"),
   DEPTH("depth-events-channel", "depth");

   private final String channelName;
   private final String topicName;

   StreamType(String channelName, String topicName) {
      this.channelName = channelName;
      this.topicName = topicName;
   }

   public static StreamType fromChannel(String channelName) {
      for (StreamType type : values()) {
         if (type.getChannelName().equals(channelName)) {
            return type;
         }
      }
      throw new IllegalArgumentException("Naməlum Redis kanalı: " + channelName);
   }

   public static StreamType fromTopicName(String topicName) {
      for (StreamType type : values()) {
         if (type.getTopicName().equalsIgnoreCase(topicName)) {
            return type;
         }
      }
      throw new IllegalArgumentException("Naməlum axın növü (Topic): " + topicName);
   }
}