package az.shlf.authservice.contants.otp;

import lombok.Getter;

@Getter
public enum OtpKeys {

   OTP_PREFIX("otp:"),
   OTP_CONFIRMED_PREFIX("otp_confirmed:"),
   OTP_ATTEMPT_PREFIX("otp_attempt:"),
   RESEND_LOCK_PREFIX("resend_lock:"),
   OTP_TOTAL_FAIL_PREFIX("otp_total_fail:");

   private final String key;

   OtpKeys(String key) {
      this.key = key;
   }
}