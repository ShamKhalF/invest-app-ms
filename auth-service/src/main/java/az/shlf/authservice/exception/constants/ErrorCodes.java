package az.shlf.authservice.exception.constants;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCodes implements MessageKeyProvider {

   VALIDATION_ERROR("error.validation", HttpStatus.BAD_REQUEST),
   USER_NOT_FOUND("error.user.not.found", HttpStatus.NOT_FOUND),
   USER_NOT_FOUND_BY_EMAIL("error.user.not.found.by.email", HttpStatus.NOT_FOUND),
   TOO_MANY_OTP_ATTEMPTS("error.too.many.otp.attempts", HttpStatus.TOO_MANY_REQUESTS),
   USER_ALREADY_EXISTS("error.user.already.exists", HttpStatus.BAD_REQUEST),

   REGISTRATION_COOLDOWN("error.registration.cooldown", HttpStatus.TOO_MANY_REQUESTS),
   OTP_EXPIRED_OR_NOT_FOUND("error.otp.expired.or.not.found", HttpStatus.NOT_FOUND),
   OTP_REVOKED_DUE_TO_FAILURES("error.otp.revoked.due.to.failures", HttpStatus.FORBIDDEN),

   ROLE_NOT_FOUND("error.role.not.found", HttpStatus.NOT_FOUND),
   PERMISSION_NOT_FOUND("error.permission.not.found", HttpStatus.NOT_FOUND),
   USERNAME_TAKEN("error.username.taken", HttpStatus.BAD_REQUEST),
   EMAIL_TAKEN("error.email.taken", HttpStatus.BAD_REQUEST),
   REFRESH_TOKEN_INVALID("error.refresh.token.invalid", HttpStatus.BAD_REQUEST),
   UNAUTHORIZED("error.unauthorized", HttpStatus.UNAUTHORIZED),
   ACCESS_DENIED("error.access.denied", HttpStatus.FORBIDDEN),
   BAD_CREDENTIALS("error.bad.credentials", HttpStatus.BAD_REQUEST),
   INTERNAL_SERVER_ERROR("error.internal.server", HttpStatus.INTERNAL_SERVER_ERROR),
   TOKEN_EXPIRED("error.token.expired", HttpStatus.UNAUTHORIZED),
   ROLE_NAME_TAKEN("error.role_name.taken", HttpStatus.BAD_REQUEST),
   PERMISSION_NAME_TAKEN("error.permission_name.taken", HttpStatus.BAD_REQUEST),
   INVALID_OTP("error.invalid.otp", HttpStatus.BAD_REQUEST),
   OTP_NOT_CONFIRMED("error.otp.not.confirmed", HttpStatus.BAD_REQUEST),
   PRIVATE_KEY_GENERATION_ERROR("error.private.key.generation", HttpStatus.INTERNAL_SERVER_ERROR),
   PUBLIC_KEY_GENERATION_ERROR("error.public.key.generation", HttpStatus.INTERNAL_SERVER_ERROR),

   // New Error Codes for AuthService
   INVALID_CREDENTIALS("error.auth.invalid.credentials", HttpStatus.UNAUTHORIZED),
   TOKEN_VALIDATION_FAILED("error.auth.token.validation.failed", HttpStatus.UNAUTHORIZED),
   REFRESH_TOKEN_NOT_FOUND_IN_CACHE("error.auth.refresh.token.not.found.in.cache", HttpStatus.UNAUTHORIZED),
   USER_FROM_TOKEN_NOT_FOUND("error.auth.user.from.token.not.found", HttpStatus.NOT_FOUND);


   private final String message;
   private final HttpStatus httpStatus;


   ErrorCodes(String message, HttpStatus httpStatus) {
      this.message = message;
      this.httpStatus = httpStatus;
   }

}