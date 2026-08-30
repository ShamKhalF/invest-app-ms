package az.shlf.authservice.exception.constants;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SuccessCode implements MessageKeyProvider {
   LOGOUT_SUCCESSFUL("success.logout", HttpStatus.OK),
   REGISTER_SUCCESSFUL("success.register", HttpStatus.OK),

   USER_REGISTERED("success.user.registered", HttpStatus.CREATED),
   USER_ADDED("success.user.added", HttpStatus.CREATED),
   USER_UPDATED("success.user.updated", HttpStatus.OK),
   USER_DEACTIVATED("success.user.deactivated", HttpStatus.OK),
   USER_DELETED("success.user.deleted", HttpStatus.OK),
   USER_ROLES_UPDATED("success.user.roles.updated", HttpStatus.OK),
   USER_PASSWORD_UPDATED("success.user.password.updated", HttpStatus.OK),
   FORGOT_PASSWORD_OTP_SENT("success.forgot.password.otp.sent", HttpStatus.OK),
   OTP_CONFIRMED("success.otp.confirmed", HttpStatus.OK),
   PASSWORD_RESET_SUCCESS("success.password.reset", HttpStatus.OK),

   ROLE_ADDED("success.role.added", HttpStatus.CREATED),
   ROLE_UPDATED("success.role.updated", HttpStatus.OK),
   ROLE_DELETED("success.role.deleted", HttpStatus.OK),
   PERMISSIONS_REMOVED_FROM_ROLE("success.permissions.removed.from.role", HttpStatus.OK),

   PERMISSION_ADDED("success.permission.added", HttpStatus.CREATED),
   PERMISSION_UPDATED("success.permission.updated", HttpStatus.OK),
   PERMISSION_DELETED("success.permission.deleted", HttpStatus.OK);

   private final String message;
   private final HttpStatus httpStatus;

   SuccessCode(String message, HttpStatus httpStatus) {
      this.message = message;
      this.httpStatus = httpStatus;
   }
}
