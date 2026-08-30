package az.shlf.authservice.util;

import java.security.*;
import java.util.Base64;

public class KeyGenerator {

   static void main(String[] args) throws NoSuchAlgorithmException {
      KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
      keyPairGenerator.initialize(2048);
      KeyPair keyPair = keyPairGenerator.generateKeyPair();

      PublicKey publicKey = keyPair.getPublic();
      PrivateKey privateKey = keyPair.getPrivate();

      String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
      String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());

      System.out.println("Açarları .env faylına aşağıdakı kimi əlavə edin:\n");
      System.out.println("JWT_PUBLIC_KEY=" + publicKeyBase64);
      System.out.println("\nJWT_PRIVATE_KEY=" + privateKeyBase64);
   }

}