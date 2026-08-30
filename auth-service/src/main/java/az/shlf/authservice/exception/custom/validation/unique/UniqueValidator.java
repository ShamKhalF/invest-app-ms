package az.shlf.authservice.exception.custom.validation.unique;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;

@RequiredArgsConstructor
public class UniqueValidator implements ConstraintValidator<UniqueField, Object> {

   private final ApplicationContext applicationContext;
   private Class<?> repositoryClass;
   private String fieldName;
   private boolean shouldExist;

   @Override
   public void initialize(UniqueField constraintAnnotation) {
      this.repositoryClass = constraintAnnotation.repository();
      this.fieldName = constraintAnnotation.fieldName();
      this.shouldExist = constraintAnnotation.exist();
   }

   @Override
   public boolean isValid(Object value, ConstraintValidatorContext context) {
      if (value == null) {
         return true;
      }

      String methodName = "existsBy" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

      try {
         Object repository = applicationContext.getBean(repositoryClass);
         Method method = repositoryClass.getMethod(methodName, value.getClass());

         Boolean existsInDb = (Boolean) method.invoke(repository, value);

         // Əgər exist() true-dursa, bazada olmalıdır.
         // Əgər exist() false-dursa, bazada olmamalıdır.
         return this.shouldExist == existsInDb;

      } catch (Exception e) {
         throw new RuntimeException("Validation failed for field: " + fieldName +
                 ". Ensure " + methodName + " method is defined in repository.", e);
      }
   }

}