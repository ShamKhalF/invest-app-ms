package az.shlf.authservice.dto.user;

import az.shlf.authservice.dto.role.RoleResponse;
import az.shlf.authservice.contants.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseAll {
   private Long id;
   private String name;
   private String surname;
   private String username;
   private String email;
   private String phone;
   private Status status;
   private Set<RoleResponse> roles;

}
