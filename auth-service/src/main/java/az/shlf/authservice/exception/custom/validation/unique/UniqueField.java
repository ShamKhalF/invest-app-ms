package az.shlf.authservice.exception.custom.validation.unique;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueValidator.class)
@Documented
public @interface UniqueField {
   String message() default "Value already exists";

   Class<?>[] groups() default {};

   Class<? extends Payload>[] payload() default {};

   Class<?> repository(); // Yoxlanılacaq Repository sinfi (məs: CustomerRepository.class)

   String fieldName();    // Entity-dəki sahə adı (məs: "email")

   boolean exist() default false;
}