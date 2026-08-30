package az.shlf.mailservice.controller;

import az.shlf.mailservice.dto.EmailEvent;
import az.shlf.mailservice.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
@Tag(name = "Mail Test", description = "Email xidmətini test etmək üçün endpoint")
public class MailTestController {

   private final EmailService emailService;

   @PostMapping("/test-send")
   @Operation(summary = "Mail göndərilməsini test edir",
           description = "Göstərilən məlumatlar və şablon tipi əsasında mail göndərir. Təhlükəsizlik tokeni (securityToken) düzgün olmalıdır.")
   public ResponseEntity<String> testSendMail(@RequestBody EmailEvent emailEvent) {
      emailService.sendEmail(emailEvent);
      return ResponseEntity.ok("Mail göndərmə əmri icra edildi.");
   }

}