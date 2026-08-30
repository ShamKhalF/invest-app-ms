package az.shlf.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BinanceExecutionReport {

   @JsonProperty("e")
   private String eventType; // "executionReport"

   @JsonProperty("X")
   private String orderStatus; // "NEW", "FILLED", "PARTIALLY_FILLED", "CANCELED"

   @JsonProperty("i")
   private Long orderId;

   @JsonProperty("s")
   private String symbol;

   @JsonProperty("S")
   private String side; // "BUY" or "SELL"

   @JsonProperty("z")
   private BigDecimal cumulativeFilledQuantity;

   @JsonProperty("Z")
   private BigDecimal cumulativeQuoteTransactedQuantity;
}