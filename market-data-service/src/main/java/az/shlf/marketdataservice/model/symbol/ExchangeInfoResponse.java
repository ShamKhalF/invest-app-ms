package az.shlf.marketdataservice.model.symbol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeInfoResponse {
    private List<SymbolInfo> symbols;
}
