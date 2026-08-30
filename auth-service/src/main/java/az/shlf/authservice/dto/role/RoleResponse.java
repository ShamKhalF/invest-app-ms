package az.shlf.authservice.dto.role;

import az.shlf.authservice.dto.permission.PermissionResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleResponse {
   private Long id;
   private String name;
   private Set<PermissionResponse> permissions;
}
