package az.shlf.apigateway.filter;

import az.shlf.apigateway.exception.constants.ErrorCodes;
import az.shlf.apigateway.exception.dto.ExceptionResponse;
import az.shlf.apigateway.service.BlacklistCacheService;
import az.shlf.apigateway.service.PermissionCacheService;
import az.shlf.apigateway.service.ResponseMessageService;
import az.shlf.apigateway.util.JwtUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

import static az.shlf.apigateway.constants.jwt.AuthKeys.*;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

   private final JwtUtils jwtUtils;
   private final PermissionCacheService permissionCacheService;
   private final BlacklistCacheService blacklistCacheService;
   private final ObjectMapper objectMapper;
   private final ResponseMessageService responseMessageService;

   public AuthenticationFilter(JwtUtils jwtUtils, PermissionCacheService permissionCacheService, BlacklistCacheService blacklistCacheService, ObjectMapper objectMapper, ResponseMessageService responseMessageService) {
      super(Config.class);
      this.jwtUtils = jwtUtils;
      this.permissionCacheService = permissionCacheService;
      this.blacklistCacheService = blacklistCacheService;
      this.objectMapper = objectMapper;
      this.responseMessageService = responseMessageService;
   }

   @Override
   public GatewayFilter apply(Config config) {
      return (exchange, chain) -> {
         ServerHttpRequest request = exchange.getRequest();

         if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
            return onError(exchange, ErrorCodes.UNAUTHORIZED);
         }

         String authHeader = Objects.requireNonNull(request.getHeaders().get(HttpHeaders.AUTHORIZATION)).getFirst();
         if (authHeader == null || !authHeader.startsWith(BEARER.getKey())) {
            return onError(exchange, ErrorCodes.UNAUTHORIZED);
         }

         String token = authHeader.substring(7);

         if (!jwtUtils.validateToken(token)) {
            return onError(exchange, ErrorCodes.UNAUTHORIZED);
         }

         Claims claims;
         try {
            claims = jwtUtils.getClaimsFromToken(token);
         } catch (Exception e) {
            return onError(exchange, ErrorCodes.UNAUTHORIZED);
         }

         if (claims.getExpiration().before(new Date())) {
            return onError(exchange, ErrorCodes.TOKEN_EXPIRED);
         }

         String tokenType = claims.get(TOKEN_TYPE.getKey(), String.class);
         if (REFRESH_TOKEN.getKey().equals(tokenType)) {
            return onError(exchange, ErrorCodes.UNAUTHORIZED);
         }

         // Yeni L2 Cache Yoxlanışı (Caffeine + Redis Async)
         String jti = claims.getId();
         if (blacklistCacheService.isBlacklisted(jti)) {
            return onError(exchange, ErrorCodes.UNAUTHORIZED);
         }

         List<String> roles = claims.get(ROLES.getKey(), List.class);

         Set<String> userPermissions = new HashSet<>();
         Map<String, Map<String, List<String>>> cache = permissionCacheService.getPermissions();
         String path = request.getURI().getPath();

         String msName = extractMsName(path);

         System.out.println("roles " + roles);
         System.out.println("cache " + cache);
         System.out.println("msName " + msName);

         if (roles != null && cache != null && msName != null) {

            // Xəta buradadır: Cache-i düzgün ardıcıllıqla çağırmalıyıq.
            // Sizin Redis Cache Strukturunuz -> Map<SERVICE_NAME, Map<ROLE_NAME, List<PERMISSION_NAME>>>
            Map<String, List<String>> rolesPermissionsForMs = cache.get(msName);

            System.out.println("rolesPermissionsForMs " + rolesPermissionsForMs);

            if (rolesPermissionsForMs != null) {
                for (String role : roles) {
                   List<String> perms = rolesPermissionsForMs.get(role);
                   System.out.println("perms "  + perms);
                   if (perms != null) {
                      userPermissions.addAll(perms);
                   }
                }
            }
         }

         System.out.println("userPermissions "  + userPermissions);

         String rolesString = roles != null ? String.join(",", roles) : "";
         String permissionsString = String.join(",", userPermissions);

         System.out.println("msName " + msName);
         System.out.println(rolesString);
         System.out.println(permissionsString);
         System.out.println(claims.getSubject());

         ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                 .headers(httpHeaders -> {
                    httpHeaders.set(X_USER_ROLES.getKey(), rolesString);
                    httpHeaders.set(X_USER_PERMISSIONS.getKey(), permissionsString);
                    httpHeaders.set(X_USERNAME.getKey(), claims.getSubject());
                 })
                 .build();

         return chain.filter(exchange.mutate().request(modifiedRequest).build());
      };
   }

   private String extractMsName(String path) {
      if (path == null || path.isEmpty()) return null;
      if (path.contains("/auth/") || path.contains("/users") || path.contains("/roles") || path.contains("/permissions")) {
         return "auth-service";
      } else if (path.contains("/api/v1/wallets") || path.contains("/wallet/")) {
         return "wallet-service";
      } else if (path.contains("/api/v1/orders") || path.contains("/order/")) {
         return "order-service";
      } else if (path.contains("/api/mail") || path.contains("/mail/")) {
         return "mail-service";
      } else if (path.contains("/api/v1/analytics") || path.contains("/api/v1/market") || path.contains("/api/v1/admin/stream") || path.contains("/market-data/")) {
         return "market-data-service";
      } else if (path.contains("/stream/") || path.contains("/ws-stream")) {
         return "stream-service";
      } else if (path.contains("/telegram/bot/")) {
         return "telegram-ai-bot";
      }

      return null;
   }

   private Mono<Void> onError(ServerWebExchange exchange, ErrorCodes errorCode) {
      ServerHttpResponse response = exchange.getResponse();
      response.setStatusCode(errorCode.getHttpStatus());
      response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

      String translatedMessage = responseMessageService.getMessage(errorCode);

      ExceptionResponse exceptionResponse = ExceptionResponse.builder()
              .status(errorCode.getHttpStatus().value())
              .code(errorCode.name())
              .message(translatedMessage)
              .timestamp(LocalDateTime.now().toString())
              .path(exchange.getRequest().getURI().getPath())
              .build();

      try {
         byte[] bytes = objectMapper.writeValueAsBytes(exceptionResponse);
         return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
      } catch (JsonProcessingException e) {
         return response.setComplete();
      }
   }

   public static class Config {
      // Put the configuration properties
   }
}