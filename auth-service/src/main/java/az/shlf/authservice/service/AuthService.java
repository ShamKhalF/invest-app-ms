package az.shlf.authservice.service;

import az.shlf.authservice.dto.request.LoginRequest;
import az.shlf.authservice.dto.response.AuthResponse;
import az.shlf.authservice.dto.response.VoidResponse;

public interface AuthService {
   AuthResponse login(LoginRequest loginRequest);

   AuthResponse refresh(String refreshToken);

   VoidResponse logout(String accessToken);
}
