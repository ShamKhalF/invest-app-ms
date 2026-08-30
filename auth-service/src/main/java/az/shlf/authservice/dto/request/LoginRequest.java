package az.shlf.authservice.dto.request;

import lombok.Data;

@Data
public class LoginRequest {
   private String usernameOrEmail;
   private String password;
}
