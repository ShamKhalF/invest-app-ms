package az.shlf.marketdataservice.model.wss;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveKlineResponse {

   private String symbol;       // s: Məsələn, "BTCUSDT"
   private String interval;     // i: Məsələn, "1m", "15m", "1h"
   private Long startTime;      // t: Şamın açılış vaxtı (millisekund)
   private Long endTime;        // T: Şamın qapanış vaxtı (millisekund)

   private String openPrice;    // o: Açılış qiyməti
   private String closePrice;   // c: Hazırki/Qapanış qiyməti (Canlı hərəkət edən budur)
   private String highPrice;    // h: Ən yüksək qiymət
   private String lowPrice;     // l: Ən aşağı qiymət
   private String volume;       // v: Həcm

   private boolean isClosed;    // x: true olarsa, deməli şam qapandı və yeni şam yaranacaq
}