package az.shlf.authservice.dto.role;

import az.shlf.authservice.exception.constants.ValidationConstants;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleRequest {
   @Size(min = 6, max = 30, message = ValidationConstants.SIZE_RANGE_6_30)
   private String name;
   private Set<Long> permissionIds;
}
