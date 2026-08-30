package az.shlf.authservice.dto.user;

import az.shlf.authservice.exception.custom.validation.valid_phone.ValidPhone;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static az.shlf.authservice.exception.constants.ValidationConstants.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {

   private String name;
   private String surname;
   @ValidPhone(message = PHONE_NOT_VALID)
   private String phone;

}
