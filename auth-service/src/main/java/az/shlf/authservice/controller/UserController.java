package az.shlf.authservice.controller;

import az.shlf.authservice.dto.response.VoidResponse;
import az.shlf.authservice.dto.user.*;
import az.shlf.authservice.security.RequirePermission;
import az.shlf.authservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

   private final UserService userService;

   @PostMapping("/register")
   public ResponseEntity<VoidResponse> registerUser(@Valid @RequestBody RegisterUserRequest request) {
      return new ResponseEntity<>(userService.registerUser(request), HttpStatus.CREATED);
   }

   @PostMapping("/confirm-registration")
   public ResponseEntity<VoidResponse> confirmRegistrationOtp(@RequestBody @Valid ConfirmOtpRequest request) {
      VoidResponse response = userService.confirmRegistrationOtp(request);
      return ResponseEntity.ok(response);
   }

   @PostMapping("/add")
   @RequirePermission("user:add")
   public ResponseEntity<VoidResponse> addUser(@Valid @RequestBody AddUserRequest request) {
      return new ResponseEntity<>(userService.addUser(request), HttpStatus.CREATED);
   }

   @PutMapping("/update/{id}")
   @RequirePermission("user:update")
   public ResponseEntity<VoidResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
      return ResponseEntity.ok(userService.updateUser(id, request));
   }

   @PatchMapping("/deactivate/{id}")
   @RequirePermission("user:deactivate")
   public ResponseEntity<VoidResponse> deActiveUser(@PathVariable Long id) {
      return ResponseEntity.ok(userService.deActiveUser(id));
   }

   @DeleteMapping("/delete/{id}")
   @RequirePermission("user:delete")
   public ResponseEntity<VoidResponse> deleteUser(@PathVariable Long id) {
      return ResponseEntity.ok(userService.deleteUser(id));
   }

   @PutMapping("/update/roles/{userId}")
   @RequirePermission("user:update_roles")
   public ResponseEntity<VoidResponse> updateUserRoles(@PathVariable Long userId, @RequestBody UpdateUserRoles request) {
      return ResponseEntity.ok(userService.updateUserRoles(userId, request));
   }

   @PutMapping("/update/password/{userId}")
   @RequirePermission("user:update_password")
   public ResponseEntity<VoidResponse> updateUserPassword(@PathVariable Long userId, @Valid @RequestBody UpdatePassword request) {
      return ResponseEntity.ok(userService.updateUserPassword(userId, request));
   }

   @PostMapping("/forgot-password")
   public ResponseEntity<VoidResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
      return ResponseEntity.ok(userService.forgotPassword(request));
   }

   @PostMapping("/forgot-password/confirm-otp")
   public ResponseEntity<VoidResponse> confirmForgotPasswordOtp(@Valid @RequestBody ConfirmOtpRequest request) {
      return ResponseEntity.ok(userService.confirmForgotPasswordOtp(request));
   }

   @PostMapping("/reset-password")
   public ResponseEntity<VoidResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
      return ResponseEntity.ok(userService.resetPassword(request));
   }

   @GetMapping("/search")
   @RequirePermission("user:read")
   public Page<UserResponse> searchUsers(@ModelAttribute SearchUserRequest request) {
      return userService.searchUsers(request);
   }

   @GetMapping("/by/{id}")
   @RequirePermission("user:read")
   public UserResponseAll userById(@PathVariable Long id) {
      return userService.userById(id);
   }
}