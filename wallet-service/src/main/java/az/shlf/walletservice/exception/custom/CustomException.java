package az.shlf.walletservice.exception.custom;

import az.shlf.walletservice.exception.constants.ErrorCodes;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CustomException extends RuntimeException {

   private final HttpStatus httpStatus;
   private final ErrorCodes errorCode;
   private Object[] args;

   public CustomException(ErrorCodes errorCode, Object... args) {
      super(errorCode.getMessage());
      this.httpStatus = errorCode.getHttpStatus();
      this.errorCode = errorCode;
      this.args = args;
   }

   public CustomException(ErrorCodes errorCode) {
      super(errorCode.getMessage());
      this.httpStatus = errorCode.getHttpStatus();
      this.errorCode = errorCode;
   }


}
