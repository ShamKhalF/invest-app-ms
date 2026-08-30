package az.shlf.authservice.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePassword {
   private String oldPassword;
   private String newPassword;
   private String confirmNewPassword;
}
