package az.shlf.authservice.dto.user;

import az.shlf.authservice.contants.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
   private Long id;
   private String name;
   private String surname;
   private String username;
   private String email;
   private String phone;
   private Status status;
}
