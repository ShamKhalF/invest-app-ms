package az.shlf.walletservice.exception.constants;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SuccessCode implements MessageKeyProvider {

   DEPOSIT_SUCCESS("success.deposit", HttpStatus.OK);;

   private final String message;
   private final HttpStatus httpStatus;

   SuccessCode(String message, HttpStatus httpStatus) {
      this.message = message;
      this.httpStatus = httpStatus;
   }
}
