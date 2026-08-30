package az.shlf.telegramaibot.exception.constants;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCodes implements MessageKeyProvider {

   VALIDATION_ERROR("error.validation", HttpStatus.BAD_REQUEST),
   INTERNAL_SERVER_ERROR("error.internal.server", HttpStatus.INTERNAL_SERVER_ERROR),
   INVALID_AMOUNT("error.invalid.amount", HttpStatus.BAD_REQUEST),

   WALLET_NOT_FOUND("error.wallet.not.found", HttpStatus.NOT_FOUND),
   INSUFFICIENT_AVAILABLE_BALANCE("error.insufficient.available.balance", HttpStatus.BAD_REQUEST),
   INSUFFICIENT_LOCKED_BALANCE("error.insufficient.locked.balance", HttpStatus.BAD_REQUEST),

   UNAUTHORIZED("error.unauthorized", HttpStatus.UNAUTHORIZED),
   ACCESS_DENIED("error.access.denied", HttpStatus.FORBIDDEN),
   BAD_CREDENTIALS("error.bad.credentials", HttpStatus.BAD_REQUEST),
   ;


   private final String message;
   private final HttpStatus httpStatus;


   ErrorCodes(String message, HttpStatus httpStatus) {
      this.message = message;
      this.httpStatus = httpStatus;
   }

}