package az.shlf.authservice.exception.custom.validation.valid_phone;

import az.shlf.authservice.util.NormalizePhoneUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {

   @Override
   public boolean isValid(String phone, ConstraintValidatorContext context) {
      if (phone == null || phone.isBlank()) {
         return true;
      }
      return NormalizePhoneUtil.normalize(phone) != null;
   }

}