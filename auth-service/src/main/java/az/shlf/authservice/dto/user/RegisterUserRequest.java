package az.shlf.authservice.dto.user;

import az.shlf.authservice.exception.custom.validation.unique.UniqueField;
import az.shlf.authservice.exception.custom.validation.valid_phone.ValidPhone;
import az.shlf.authservice.repository.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static az.shlf.authservice.contants.fields.ValidationFieldNamesUtil.EMAIL;
import static az.shlf.authservice.contants.fields.ValidationFieldNamesUtil.USERNAME;
import static az.shlf.authservice.exception.constants.ValidationConstants.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterUserRequest {

   @Size(min = 3, max = 30, message = SIZE_RANGE_3_30)
   @NotBlank(message = NOT_BLANK)
   private String name;

   @Size(min = 3, max = 30, message = SIZE_RANGE_3_30)
   @NotBlank(message = NOT_BLANK)
   private String surname;

   @Size(min = 3, max = 30, message = SIZE_RANGE_3_30)
   @NotBlank(message = NOT_BLANK)
   @UniqueField(fieldName = USERNAME, repository = UserRepository.class, message = UNIQUE_FIELD)
   private String username;

   @NotBlank(message = NOT_BLANK)
   @Email(message = EMAIL_NOT_VALID)
   @UniqueField(fieldName = EMAIL, repository = UserRepository.class, message = UNIQUE_FIELD)
   private String email;

   @Size(min = 6, max = 30, message = SIZE_RANGE_6_30)
   @NotBlank(message = NOT_BLANK)
   private String password;

   @NotBlank(message = PHONE_NOT_VALID)
   @ValidPhone(message = PHONE_NOT_VALID)
   private String phone;

}
