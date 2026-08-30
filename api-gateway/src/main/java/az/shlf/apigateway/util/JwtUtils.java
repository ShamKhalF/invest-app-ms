package az.shlf.apigateway.util;

import az.shlf.apigateway.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class JwtUtils {

   private final JwtProperties jwtProperties;

   public Claims getClaimsFromToken(String token) {
      return Jwts.parser()
              .verifyWith(getPublicKey())
              .build()
              .parseSignedClaims(token)
              .getPayload();
   }

   public boolean validateToken(String token) {
      try {
         getClaimsFromToken(token);
         return true;
      } catch (Exception e) {
         return false;
      }
   }

   private PublicKey getPublicKey() {
      try {
         byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getPublicKey());
         X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
         KeyFactory kf = KeyFactory.getInstance("RSA");
         return kf.generatePublic(spec);
      } catch (Exception e) {
         throw new RuntimeException("Public key generation error");
      }
   }

}