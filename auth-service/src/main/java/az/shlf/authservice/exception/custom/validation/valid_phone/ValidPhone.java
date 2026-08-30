package az.shlf.authservice.exception.custom.validation.valid_phone;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PhoneValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone {
   String message() default "Invalid number format."; // Default xəta mesajı

   Class<?>[] groups() default {};

   Class<? extends Payload>[] payload() default {};
}