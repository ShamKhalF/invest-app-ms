package az.shlf.authservice.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRoles {
   private Set<Long> addedRoleIds;
   private Set<Long> removedRoleIds;
}
