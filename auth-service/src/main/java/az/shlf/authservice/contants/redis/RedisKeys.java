package az.shlf.authservice.contants.redis;

import lombok.Getter;

@Getter
public enum RedisKeys {

   GROUPED_PERMISSIONS_KEY("grouped_permissions"),
   PERMISSIONS_CHANNEL("permissions_channel"),


   REFRESH_TOKEN_PREFIX("REFRESH_TOKEN:"),
   BLACKLIST_PREFIX("BLACKLIST:"),
   UPDATED_ROLES_PREFIX("UPDATED_ROLES:");


   private final String key;

   RedisKeys(String key) {
      this.key = key;
   }

}
