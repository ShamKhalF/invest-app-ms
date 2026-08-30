package az.shlf.marketdataservice.model.wss;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepthEntry {
   private String price;    // Qiymət
   private String quantity; // Məbləğ (Koin sayı)
}