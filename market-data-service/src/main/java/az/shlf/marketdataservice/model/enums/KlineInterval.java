package az.shlf.marketdataservice.model.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum KlineInterval {
   // Dəqiqəlik
   ONE_MINUTE("1m", 60L),
   THREE_MINUTES("3m", 180L),
   FIVE_MINUTES("5m", 300L),
   FIFTEEN_MINUTES("15m", 900L),
   THIRTY_MINUTES("30m", 1800L),

   // Saatlıq
   ONE_HOUR("1h", 3600L),
   TWO_HOURS("2h", 7200L),
   FOUR_HOURS("4h", 14400L),
   SIX_HOURS("6h", 21600L),
   EIGHT_HOURS("8h", 28800L),
   TWELVE_HOURS("12h", 43200L),

   // Gün / Həftə / Ay
   ONE_DAY("1d", 86400L),
   THREE_DAYS("3d", 259200L),
   ONE_WEEK("1w", 604800L),
   ONE_MONTH("1M", 2592000L); // 30 gün olaraq hesablanır

   private final String value;
   private final long durationInSeconds;

   KlineInterval(String value, long durationInSeconds) {
      this.value = value;
      this.durationInSeconds = durationInSeconds;
   }

   // JSON obyektinə çevirəndə (Serialize) yalnız "1m" stringi kimi oxunması üçün
   @JsonValue
   public String getValue() {
      return value;
   }

   // JSON-dan (Kafka/REST) gələn "1m" stringini Enum obyektinə çevirmək üçün
   @JsonCreator
   public static KlineInterval fromValue(String value) {
      for (KlineInterval interval : values()) {
         if (interval.value.equals(value)) {
            return interval;
         }
      }
      throw new IllegalArgumentException("Geçərsiz (Invalid) Kline intervalı: " + value);
   }
}