package az.shlf.authservice.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailEvent {
   private String email;
   private String otp;
   private EmailType type;
   private String securityToken; // Təhlükəsizlik şifrəsi

}