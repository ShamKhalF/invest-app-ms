package az.shlf.orderservice.exception.constants;

import az.shlf.orderservice.exception.constants.MessageKeyProvider;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SuccessCode implements MessageKeyProvider {

   ORDER_CANCELED("success.order.canceled", HttpStatus.OK),

   ;

   private final String message;
   private final HttpStatus httpStatus;

   SuccessCode(String message, HttpStatus httpStatus) {
      this.message = message;
      this.httpStatus = httpStatus;
   }
}
