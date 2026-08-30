package az.shlf.authservice.controller;

import az.shlf.authservice.dto.request.LoginRequest;
import az.shlf.authservice.dto.response.AuthResponse;
import az.shlf.authservice.dto.response.VoidResponse;
import az.shlf.authservice.exception.constants.ErrorCodes;
import az.shlf.authservice.exception.custom.CustomException;
import az.shlf.authservice.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static az.shlf.authservice.contants.jwt.AuthKeys.BEARER;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

   private final AuthService authService;

   @PostMapping("/login")
   public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
      return ResponseEntity.ok(authService.login(request));
   }

   @PostMapping("/refresh")
   public ResponseEntity<AuthResponse> refresh(@RequestParam String token) {
      return ResponseEntity.ok(authService.refresh(token));
   }

   @PostMapping("/logout")
   public ResponseEntity<VoidResponse> logout(HttpServletRequest request) {
      String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

      if (authHeader == null || !authHeader.startsWith("Bearer ")) {
         throw new CustomException(ErrorCodes.TOKEN_VALIDATION_FAILED);
      }

      String token = authHeader.substring(7);
      return ResponseEntity.ok(authService.logout(token));
   }

}
