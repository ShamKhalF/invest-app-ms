package az.shlf.marketdataservice.dto;

import lombok.Data;

@Data
public class TopSymbolSearchDto {
   private String keyword;
   private int page = 0;
   private int size = 10;
}