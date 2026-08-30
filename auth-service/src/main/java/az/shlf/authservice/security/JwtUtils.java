package az.shlf.authservice.security;

import az.shlf.authservice.config.properties.JwtProperties;
import az.shlf.authservice.exception.constants.ErrorCodes;
import az.shlf.authservice.exception.custom.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static az.shlf.authservice.contants.jwt.AuthKeys.*;

@Component
@RequiredArgsConstructor
public class JwtUtils {

   private final JwtProperties jwtProperties;

   public String generateAccessToken(String username, List<String> roles, String jti) {
      return Jwts.builder()
              .subject(username)
              .claim(ROLES.getKey(), roles)
              .claim(TOKEN_TYPE.getKey(), ACCESS_TOKEN.getKey())
              .id(jti)
              .issuer(ISSUER.getKey())
              .issuedAt(new Date())
              .expiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpiration()))
              .signWith(getPrivateKey(), Jwts.SIG.RS256)
              .compact();
   }

   public String generateRefreshToken(String username, String jti) {
      return Jwts.builder()
              .subject(username)
              .claim(TOKEN_TYPE.getKey(), REFRESH_TOKEN.getKey())
              .id(jti)
              .issuer(ISSUER.getKey())
              .issuedAt(new Date())
              .expiration(new Date(System.currentTimeMillis() + jwtProperties.getRefreshTokenExpiration()))
              .signWith(getPrivateKey(), Jwts.SIG.RS256)
              .compact();
   }

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

   private PrivateKey getPrivateKey() {
      try {
         byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getPrivateKey());
         PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
         KeyFactory kf = KeyFactory.getInstance("RSA");
         return kf.generatePrivate(spec);
      } catch (Exception e) {
         throw new CustomException(ErrorCodes.PRIVATE_KEY_GENERATION_ERROR);
      }
   }

   private PublicKey getPublicKey() {
      try {
         byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getPublicKey());
         X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
         KeyFactory kf = KeyFactory.getInstance("RSA");
         return kf.generatePublic(spec);
      } catch (Exception e) {
         throw new CustomException(ErrorCodes.PUBLIC_KEY_GENERATION_ERROR);
      }
   }
}