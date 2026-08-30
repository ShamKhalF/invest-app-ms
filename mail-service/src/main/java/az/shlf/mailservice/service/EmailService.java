package az.shlf.mailservice.service;

import az.shlf.mailservice.dto.EmailEvent;
import az.shlf.mailservice.selector.EmailTemplateSelector;
import az.shlf.mailservice.selector.EmailTemplateSelector.TemplateContext;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

   private final JavaMailSender mailSender;
   private final TemplateEngine templateEngine;
   private final EmailTemplateSelector templateSelector;

   @Value("${spring.mail.username}")
   private String fromEmail;

   @Value("${app.security.mail-token}")
   private String expectedToken;

   public void sendEmail(EmailEvent event) {
      // Təhlükəsizlik yoxlanışı
      if (event.getSecurityToken() == null || !event.getSecurityToken().equals(expectedToken)) {
         log.error("İcazəsiz mail göndərmə cəhdi. Təqdim edilən token yanlışdır. Email: {}", event.getEmail());
         return; // Və ya Exception ata bilərsiniz
      }

      try {
         TemplateContext templateInfo = templateSelector.getTemplateContext(event.getType());

         Context context = new Context();
         if (event.getOtp() != null) {
            context.setVariable("otp", event.getOtp());
         }

         String htmlContent = templateEngine.process(templateInfo.getTemplateName(), context);

         MimeMessage message = mailSender.createMimeMessage();
         MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

         helper.setFrom(fromEmail);
         helper.setTo(event.getEmail());
         helper.setSubject(templateInfo.getSubject());
         helper.setText(htmlContent, true);

         mailSender.send(message);
         log.info("Email uğurla göndərildi: {}", event.getEmail());

      } catch (MessagingException e) {
         log.error("Email göndərilməsində xəta baş verdi. Email: {}", event.getEmail(), e);
         throw new RuntimeException("Email server ilə əlaqə qurula bilmədi", e);
      }
   }

}