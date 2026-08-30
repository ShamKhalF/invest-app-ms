package az.shlf.marketdataservice.model.wss;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveDepthResponse {
   private String symbol;
   private List<DepthEntry> bids; // Alıcılar (Yaşıl siyahı)
   private List<DepthEntry> asks; // Satıcılar (Qırmızı siyahı)
}
