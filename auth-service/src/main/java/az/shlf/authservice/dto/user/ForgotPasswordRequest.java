package az.shlf.authservice.dto.user;

import az.shlf.authservice.exception.constants.ValidationConstants;
import az.shlf.authservice.exception.custom.validation.unique.UniqueField;
import az.shlf.authservice.repository.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import static az.shlf.authservice.contants.fields.ValidationFieldNamesUtil.EMAIL;
import static az.shlf.authservice.exception.constants.ValidationConstants.*;

@Data
public class ForgotPasswordRequest {
   @NotBlank(message = NOT_BLANK)
   @Email(message = EMAIL_NOT_VALID)
   @UniqueField(fieldName = EMAIL, repository = UserRepository.class, message = NOT_EXIST_FIELD, exist = true)
   private String email;
}
