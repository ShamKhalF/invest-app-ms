package az.shlf.authservice.dto.permission;

import az.shlf.authservice.exception.constants.ValidationConstants;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionRequest {
   @Size(min = 6, max = 30, message = ValidationConstants.SIZE_RANGE_6_30)
   private String name;
   @Size(min = 3, max = 30, message = ValidationConstants.SIZE_RANGE_3_30)
   private String service;
}
