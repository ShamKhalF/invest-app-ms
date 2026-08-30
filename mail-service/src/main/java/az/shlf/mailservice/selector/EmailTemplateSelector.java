package az.shlf.mailservice.selector;

import az.shlf.mailservice.dto.EmailType;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplateSelector {

   @Data
   @AllArgsConstructor
   public static class TemplateContext {
      private String subject;
      private String templateName;
   }

   public TemplateContext getTemplateContext(EmailType type) {
      return switch (type) {
         case REGISTRATION_OTP -> new TemplateContext("Hesabın Təsdiqlənməsi", "registration-otp");
         case WELCOME -> new TemplateContext("Xoş Gəldiniz!", "welcome");
         case FORGOT_PASSWORD_OTP -> new TemplateContext("Şifrənin Yenilənməsi", "forgot-password-otp");
         default -> throw new IllegalArgumentException("Dəstəklənməyən email tipi: " + type);
      };
   }
}