package az.shlf.authservice.dto.user;

import az.shlf.authservice.exception.constants.ValidationConstants;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConfirmOtpRequest {
   @NotBlank(message = ValidationConstants.NOT_BLANK)
   @Email(message = ValidationConstants.EMAIL_NOT_VALID)
   private String email;

   @NotBlank(message = ValidationConstants.NOT_BLANK)
   @Size(min = 4, max = 4, message = ValidationConstants.SIZE_4)
   private String otp;
}
