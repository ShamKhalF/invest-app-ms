package az.shlf.apigateway.exception.constants;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCodes {

   UNAUTHORIZED("error.unauthorized", HttpStatus.UNAUTHORIZED),
   TOKEN_EXPIRED("error.token.expired", HttpStatus.UNAUTHORIZED),
   INTERNAL_SERVER_ERROR("error.internal.server", HttpStatus.INTERNAL_SERVER_ERROR);

   private final String message;
   private final HttpStatus httpStatus;

   ErrorCodes(String message, HttpStatus httpStatus) {
      this.message = message;
      this.httpStatus = httpStatus;
   }
}