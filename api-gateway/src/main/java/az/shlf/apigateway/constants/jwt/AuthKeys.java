package az.shlf.apigateway.constants.jwt;

import lombok.Getter;

@Getter
public enum AuthKeys {

   BEARER("Bearer "),
   AUTHORIZATION("Authorization"),
   TOKEN_TYPE("tokenType"),
   ACCESS_TOKEN("accessToken"),
   REFRESH_TOKEN("refreshToken"),
   ROLES("roles"),
   ISSUER("invest-app"),
   X_USERNAME("X-Username"),
   X_USER_ROLES("X-User-Roles"),
   X_USER_PERMISSIONS("X-User-Permissions");

   private final String key;

   AuthKeys(String key) {
      this.key = key;
   }

}