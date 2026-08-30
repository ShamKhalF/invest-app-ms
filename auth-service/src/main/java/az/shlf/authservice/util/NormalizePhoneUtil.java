package az.shlf.authservice.util;

import java.util.Set;

public class NormalizePhoneUtil {

   private static final Set<String> VALID_PREFIXES = Set.of(
           "10", "50", "51", "55", "70", "77", "99"
   );
   private static final String AZE_CODE = "+994";

   private NormalizePhoneUtil() {
   }


   public static String normalize(String phone) {
      if (phone == null || phone.isBlank()) {
         return null;
      }

      String digitsOnly = phone.replaceAll("\\D", "");

      if (digitsOnly.length() < 9) {
         return null;
      }

      String last9Digits = digitsOnly.substring(digitsOnly.length() - 9);

      String prefix = last9Digits.substring(0, 2);

      if (!VALID_PREFIXES.contains(prefix)) {
         return null;
      }

      return AZE_CODE + last9Digits;
   }

}
