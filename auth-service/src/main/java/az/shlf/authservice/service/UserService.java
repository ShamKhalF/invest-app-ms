package az.shlf.authservice.service;

import az.shlf.authservice.dto.response.VoidResponse;
import az.shlf.authservice.dto.user.*;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

public interface UserService {
   VoidResponse registerUser(RegisterUserRequest request);

   VoidResponse confirmRegistrationOtp(ConfirmOtpRequest request);

   VoidResponse addUser(AddUserRequest request);

   VoidResponse updateUser(Long id, UpdateUserRequest request);

   VoidResponse deActiveUser(Long id);

   VoidResponse deleteUser(Long id);

   VoidResponse updateUserRoles(Long userId, UpdateUserRoles request);

   VoidResponse updateUserPassword(Long userId, UpdatePassword request);

   VoidResponse forgotPassword(ForgotPasswordRequest request);

   VoidResponse confirmForgotPasswordOtp(ConfirmOtpRequest request);

   VoidResponse resetPassword(ResetPasswordRequest request);

   Page<UserResponse> searchUsers(SearchUserRequest request);

   UserResponseAll userById(Long id);
}
