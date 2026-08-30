package az.shlf.marketdataservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "symbol_hourly_klines", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"symbol", "close_time"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SymbolHourlyKlineEntity {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(name = "symbol", length = 20, nullable = false)
   private String symbol;

   @Column(name = "open_time", nullable = false)
   private Long openTime;

   @Column(name = "close_time", nullable = false)
   private Long closeTime;

   @Column(name = "open_price", precision = 36, scale = 18)
   private BigDecimal openPrice;

   @Column(name = "high_price", precision = 36, scale = 18)
   private BigDecimal highPrice;

   @Column(name = "low_price", precision = 36, scale = 18)
   private BigDecimal lowPrice;

   @Column(name = "close_price", precision = 36, scale = 18)
   private BigDecimal closePrice;

   @Column(name = "volume", precision = 36, scale = 18)
   private BigDecimal volume;
}