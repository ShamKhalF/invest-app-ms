package az.shlf.walletservice.listener.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletCreateEvent {
   private String username;
   private String email;
}
