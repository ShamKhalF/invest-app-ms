package az.shlf.marketdataservice.model.kline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KlineResponse {

   // --- STANDART ŞAM MƏLUMATLARI (Qrafik Çəkmək Üçün) ---

   private Long openTime;       // [0] Şamın açılış zamanı (millisekundla)
   private String openPrice;    // [1] Şam açılanda koinin ilk qiyməti (Open)
   private String highPrice;    // [2] Bu zaman aralığında koinin çatdığı ən yüksək qiymət (High)
   private String lowPrice;     // [3] Bu zaman aralığında koinin düşdüyü ən aşağı qiymət (Low)
   private String closePrice;   // [4] Şam qapananda koinin son qiyməti (Close)
   private String volume;       // [5] Base Asset həcmi (Məs: Neçə dənə BTC alınıb-satılıb)
   private Long closeTime;      // [6] Şamın qapanış zamanı (millisekundla)

   // --- DƏRİN ANALİZ VƏ DATA MİNİNG MƏLUMATLARI ---

   private String quoteAssetVolume;      // [7] Quote Asset həcmi (Məs: Dövr edən ümumi USDT miqdarı)
   private Integer numberOfTrades;       // [8] Reallaşan unikal alqı-satqı əməliyyatlarının sayı
   private String takerBuyBaseVolume;    // [9] "Taker"lərin aldığı Base Asset həcmi (Aqressiv alınan BTC miqdarı)
   private String takerBuyQuoteVolume;   // [10] "Taker"lərin xərclədiyi Quote Asset həcmi (Aqressiv alışlara gedən USDT)

   // [11] ignore sahəsidir deyə DTO-ya əlavə etmirik.
}