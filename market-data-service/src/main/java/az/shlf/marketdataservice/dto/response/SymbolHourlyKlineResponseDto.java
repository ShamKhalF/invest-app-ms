package az.shlf.marketdataservice.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SymbolHourlyKlineResponseDto {
   private Long id;
   private String symbol;
   private LocalDateTime openTime;
   private LocalDateTime closeTime;
   private BigDecimal openPrice;
   private BigDecimal highPrice;
   private BigDecimal lowPrice;
   private BigDecimal closePrice;
   private BigDecimal volume;
}