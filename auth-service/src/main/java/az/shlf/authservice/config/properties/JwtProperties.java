package az.shlf.authservice.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jwt")
@Getter
@Setter
public class JwtProperties {
   private String publicKey;
   private String privateKey;
   private long accessTokenExpiration;
   private long refreshTokenExpiration;
}