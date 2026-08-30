package az.shlf.marketdataservice.exception.constants;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCodes implements MessageKeyProvider {

   VALIDATION_ERROR("error.validation", HttpStatus.BAD_REQUEST),

   INVALID_AMOUNT("error.invalid.amount", HttpStatus.BAD_REQUEST),
   BALANCE_RESERVE_FAILED("error.balance.reserve.failed", HttpStatus.BAD_REQUEST),
   BINANCE_EXECUTION_FAILED("error.binance.execution.failed", HttpStatus.INTERNAL_SERVER_ERROR),

   UNAUTHORIZED("error.unauthorized", HttpStatus.UNAUTHORIZED),
   ACCESS_DENIED("error.access.denied", HttpStatus.FORBIDDEN),
   BAD_CREDENTIALS("error.bad.credentials", HttpStatus.BAD_REQUEST),
   INTERNAL_SERVER_ERROR("error.internal.server", HttpStatus.INTERNAL_SERVER_ERROR),

   ORDER_NOT_FOUND("error.order.not.found", HttpStatus.NOT_FOUND),
   ORDER_NOT_OPEN("error.order.not.open", HttpStatus.BAD_REQUEST),
   BINANCE_CANCEL_FAILED("error.binance.cancel.failed", HttpStatus.INTERNAL_SERVER_ERROR),
   BALANCE_RELEASE_FAILED("error.balance.release.failed", HttpStatus.INTERNAL_SERVER_ERROR),

   BALANCE_COMMIT_FAILED("error.balance.commit.failed", HttpStatus.INTERNAL_SERVER_ERROR),

   ;

   private final String message;
   private final HttpStatus httpStatus;


   ErrorCodes(String message, HttpStatus httpStatus) {
      this.message = message;
      this.httpStatus = httpStatus;
   }

}