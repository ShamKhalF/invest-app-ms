package az.shlf.marketdataservice.model.symbol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SymbolInfo {
    private String symbol;
    private String status;
    private String baseAsset;
    private String quoteAsset;
}
