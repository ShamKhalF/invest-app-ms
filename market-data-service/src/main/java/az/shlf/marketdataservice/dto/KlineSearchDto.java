package az.shlf.marketdataservice.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class KlineSearchDto {
   private String symbol;
   private BigDecimal minPrice;
   private BigDecimal maxPrice;
   private Long startTime;
   private Long endTime;
   private int page = 0;
   private int size = 20;
}