package az.shlf.marketdataservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopWatchedSymbolResponseDto {
   private String symbol;
   private String name;
   private Long watchCount;
}