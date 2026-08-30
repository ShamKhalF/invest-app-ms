package az.shlf.authservice.service.impl;

import az.shlf.authservice.contants.entity.Status;
import az.shlf.authservice.dto.request.LoginRequest;
import az.shlf.authservice.dto.response.AuthResponse;
import az.shlf.authservice.dto.response.VoidResponse;
import az.shlf.authservice.entity.Role;
import az.shlf.authservice.entity.User;
import az.shlf.authservice.exception.constants.ErrorCodes;
import az.shlf.authservice.exception.constants.SuccessCode;
import az.shlf.authservice.exception.custom.CustomException;
import az.shlf.authservice.repository.UserRepository;
import az.shlf.authservice.security.JwtUtils;
import az.shlf.authservice.service.AuthService;
import az.shlf.authservice.service.RedisService;
import az.shlf.authservice.util.ResponseMessageHelper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static az.shlf.authservice.contants.jwt.AuthKeys.ACCESS_TOKEN;
import static az.shlf.authservice.contants.jwt.AuthKeys.TOKEN_TYPE;
import static az.shlf.authservice.contants.redis.RedisKeys.*;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

   private final UserRepository userRepository;
   private final PasswordEncoder passwordEncoder;
   private final JwtUtils jwtUtils;
   private final RedisService redisService;
   private final ResponseMessageHelper messageHelper;


   @Override
   @Transactional(readOnly = true)
   public AuthResponse login(LoginRequest loginRequest) {

      List<Status> statuses = List.of(Status.ACTIVE, Status.INACTIVE);
      User user = userRepository.findByUsernameOrEmailAndStatusIn(loginRequest.getUsernameOrEmail(), loginRequest.getUsernameOrEmail(), statuses)
              .orElseThrow(() -> new CustomException(ErrorCodes.USER_NOT_FOUND, loginRequest.getUsernameOrEmail()));

      if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
         throw new CustomException(ErrorCodes.INVALID_CREDENTIALS);
      }

      List<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
      String jti = UUID.randomUUID().toString();
      String accessToken = jwtUtils.generateAccessToken(user.getUsername(), roles, jti);
      String refreshToken = jwtUtils.generateRefreshToken(user.getUsername(), jti);

      redisService.set(REFRESH_TOKEN_PREFIX.getKey() + jti, roles, 7, TimeUnit.DAYS);

      return AuthResponse.builder()
              .accessToken(accessToken)
              .refreshToken(refreshToken)
              .build();
   }

   @Override
   @SuppressWarnings("unchecked")
   public AuthResponse refresh(String refreshToken) {
      if (!jwtUtils.validateToken(refreshToken)) {
         throw new CustomException(ErrorCodes.REFRESH_TOKEN_INVALID);
      }

      Claims claims = jwtUtils.getClaimsFromToken(refreshToken);
      String tokenType = claims.get(TOKEN_TYPE.getKey(), String.class);
      if (ACCESS_TOKEN.getKey().equals(tokenType)) {
         throw new CustomException(ErrorCodes.REFRESH_TOKEN_INVALID); // or custom exception for invalid token type
      }

      String oldJti = claims.getId();
      String username = claims.getSubject();

      // `get` metoduna parametr kimi List.class əlavə edilib
      List<String> roles = (List<String>) redisService.get(REFRESH_TOKEN_PREFIX.getKey() + oldJti, List.class);
      if (roles == null) {
         throw new CustomException(ErrorCodes.REFRESH_TOKEN_NOT_FOUND_IN_CACHE);
      }

      // `get` metoduna parametr kimi Object.class əlavə edilib
      if (redisService.get(UPDATED_ROLES_PREFIX.getKey() + username, Object.class) != null) {
         User user = userRepository.findByUsername(username).orElseThrow(() -> new CustomException(ErrorCodes.USER_FROM_TOKEN_NOT_FOUND));
         roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
         redisService.delete(UPDATED_ROLES_PREFIX.getKey() + username);
      }

      redisService.delete(REFRESH_TOKEN_PREFIX.getKey() + oldJti);
      redisService.set(BLACKLIST_PREFIX.getKey() + oldJti, "", 15, TimeUnit.MINUTES);


      String newJti = UUID.randomUUID().toString();
      String newAccessToken = jwtUtils.generateAccessToken(username, roles, newJti);
      String newRefreshToken = jwtUtils.generateRefreshToken(username, newJti);

      redisService.set(REFRESH_TOKEN_PREFIX.getKey() + newJti, roles, 7, TimeUnit.DAYS);

      return AuthResponse.builder()
              .accessToken(newAccessToken)
              .refreshToken(newRefreshToken)
              .build();
   }

   @Override
   public VoidResponse logout(String accessToken) {
      if (!jwtUtils.validateToken(accessToken)) {
         throw new CustomException(ErrorCodes.TOKEN_VALIDATION_FAILED);
      }

      Claims claims = jwtUtils.getClaimsFromToken(accessToken);
      String tokenType = claims.get(TOKEN_TYPE.getKey(), String.class);
      if (!ACCESS_TOKEN.getKey().equals(tokenType)) {
          throw new CustomException(ErrorCodes.TOKEN_VALIDATION_FAILED);
      }

      String jti = claims.getId();
      long exp = claims.getExpiration().getTime();
      long ttl = exp - System.currentTimeMillis();

      redisService.delete(REFRESH_TOKEN_PREFIX.getKey() + jti);
      redisService.set(BLACKLIST_PREFIX.getKey() + jti, "", ttl, TimeUnit.MILLISECONDS);
      return messageHelper.getVoidResponse(SuccessCode.LOGOUT_SUCCESSFUL);
   }
}