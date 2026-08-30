package az.shlf.marketdataservice.model.enums;

public enum ChartTimeFrame {
   ONE_DAY(86400L),          // 1 Günlük qrafik
   ONE_WEEK(604800L),        // 1 Həftəlik qrafik
   ONE_MONTH(2592000L),      // 1 Aylıq qrafik (30 gün)
   THREE_MONTHS(7776000L),   // 3 Aylıq qrafik (90 gün)
   SIX_MONTHS(15552000L),    // 6 Aylıq qrafik (180 gün)
   ONE_YEAR(31536000L);      // 1 İllik qrafik (365 gün)

   private final long rangeInSeconds;

   ChartTimeFrame(long rangeInSeconds) {
      this.rangeInSeconds = rangeInSeconds;
   }

   /**
    * Verilən zaman aralığını seçilmiş intervala bölərək Binance üçün lazımi "limit" dəyərini tapır.
    * Məsələn: ONE_YEAR (365 gün) və ONE_WEEK (7 gün) daxil edilsə -> 365/7 = 52 qaytarır.
    */
   public int calculateLimit(KlineInterval interval) {
      long limit = this.rangeInSeconds / interval.getDurationInSeconds();

      // 1. Təhlükəsizlik: Limit minimum 1 olmalıdır
      if (limit < 1) {
         return 1;
      }
      // 2. Binance API Məhdudiyyəti: Binance bir sorğuda maksimum 1000 şam qaytarır
      if (limit > 1000) {
         return 1000;
      }

      return (int) limit;
   }
}