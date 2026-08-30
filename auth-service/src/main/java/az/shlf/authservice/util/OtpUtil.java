package az.shlf.authservice.util;

import java.security.SecureRandom;

public class OtpUtil {

   private static final SecureRandom random = new SecureRandom();

   /**
    * Generates a 6-digit random OTP.
    *
    * @return A 6-digit OTP as a String.
    */
   public static String generate6DigitOtp() {
      int otp = 100000 + random.nextInt(900000);
      return String.valueOf(otp);
   }

   public static String generate4DigitOtp() {
      int otp = 1000 + random.nextInt(9000);
      return String.valueOf(otp);
   }


}
