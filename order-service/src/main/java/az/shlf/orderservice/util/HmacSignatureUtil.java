package az.shlf.orderservice.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;

public class HmacSignatureUtil {
   public static String generateSignature(String data, String secret) {
      try {
         Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
         SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
         sha256_HMAC.init(secret_key);
         return Hex.encodeHexString(sha256_HMAC.doFinal(data.getBytes()));
      } catch (Exception e) {
         throw new RuntimeException("HMAC imza xətası", e);
      }
   }
}