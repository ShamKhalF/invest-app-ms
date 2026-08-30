package az.shlf.telegramaibot.exception.constants;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SuccessCode implements MessageKeyProvider {

   TELEGRAM_LINK_GENERATED("success.telegram.link.generated", HttpStatus.OK),
   TELEGRAM_DELETE_OTP_SENT("success.telegram.delete.otp.sent", HttpStatus.OK),
   TELEGRAM_USER_DELETED("success.telegram.user.deleted", HttpStatus.OK);

   private final String message;
   private final HttpStatus httpStatus;

   SuccessCode(String message, HttpStatus httpStatus) {
      this.message = message;
      this.httpStatus = httpStatus;
   }
}
