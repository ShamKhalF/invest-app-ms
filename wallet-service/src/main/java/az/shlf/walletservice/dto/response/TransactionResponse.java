package az.shlf.walletservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
   private String asset;
   private String type;
   private BigDecimal amount;
   private String referenceId;
   private LocalDateTime createdAt;
}
