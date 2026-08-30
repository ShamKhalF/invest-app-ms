package az.shlf.authservice.service.impl;

import az.shlf.authservice.dto.response.VoidResponse;
import az.shlf.authservice.dto.user.*;
import az.shlf.authservice.dto.kafka.EmailEvent;
import az.shlf.authservice.dto.kafka.EmailType;
import az.shlf.authservice.dto.kafka.WalletCreateEvent;
import az.shlf.authservice.entity.Role;
import az.shlf.authservice.entity.User;
import az.shlf.authservice.contants.entity.Status;
import az.shlf.authservice.contants.otp.OtpKeys;
import az.shlf.authservice.exception.constants.ErrorCodes;
import az.shlf.authservice.exception.constants.SuccessCode;
import az.shlf.authservice.exception.custom.CustomException;
import az.shlf.authservice.mapper.UserMapper;
import az.shlf.authservice.repository.RoleRepository;
import az.shlf.authservice.repository.UserRepository;
import az.shlf.authservice.service.KafkaProducerService;
import az.shlf.authservice.service.RedisService;
import az.shlf.authservice.service.UserService;
import az.shlf.authservice.specifications.UserSpecification;
import az.shlf.authservice.util.OtpUtil;
import az.shlf.authservice.util.PageableCheckUtil;
import az.shlf.authservice.util.ResponseMessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static az.shlf.authservice.contants.redis.RedisKeys.UPDATED_ROLES_PREFIX;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

   private final UserRepository userRepository;
   private final UserMapper userMapper;
   private final PasswordEncoder passwordEncoder;
   private final RoleRepository roleRepository;
   private final UserSpecification userSpecification;
   private final RedisService redisService;
   private final ResponseMessageHelper messageHelper;
   private final KafkaProducerService kafkaProducerService; // Kafka servisi inyeksiya edildi

   @Value("${app.security.mail-token}")
   private String mailSecurityToken;

   @Override
   @Transactional
   public VoidResponse registerUser(RegisterUserRequest request) {
      String lockKey = OtpKeys.RESEND_LOCK_PREFIX.getKey() + request.getEmail();
      String lockValue = redisService.get(lockKey, String.class);

      // B. Spam qorunması: 2 dəqiqə ərzində yenidən sorğu göndərilibsə, bloklanır
      if (lockValue != null) {
         throw new CustomException(ErrorCodes.REGISTRATION_COOLDOWN);
      }

      Optional<User> existingUserOpt = userRepository.findByEmail(request.getEmail());
      User user;

      if (existingUserOpt.isPresent()) {
         user = existingUserOpt.get();

         if (user.getStatus() == Status.ACTIVE) {
            throw new CustomException(ErrorCodes.USER_ALREADY_EXISTS);
         }

         user.setPassword(passwordEncoder.encode(request.getPassword()));
         // Ehtiyac varsa, digər sahələri də yeniləyin
      } else {
         user = userMapper.toEntityRegister(request);
         user.setPassword(passwordEncoder.encode(request.getPassword()));
         user.setStatus(Status.NOT_CONFIRMED);

         Role customerRole = roleRepository.findByName("CUSTOMER")
                 .orElseThrow(() -> new CustomException(ErrorCodes.ROLE_NOT_FOUND));

         user.setRoles(Set.of(customerRole));

      }

      userRepository.save(user);

      String otp = OtpUtil.generate4DigitOtp();
      redisService.set(OtpKeys.OTP_PREFIX.getKey() + user.getEmail(), otp, 10, TimeUnit.MINUTES);

      // Növbəti sorğunu bloklamaq üçün 2 dəqiqəlik "cooldown" təyin edilir
      redisService.set(lockKey, "LOCKED", 2, TimeUnit.MINUTES);

      EmailEvent emailEvent = new EmailEvent(user.getEmail(), otp, EmailType.REGISTRATION_OTP, mailSecurityToken);
      kafkaProducerService.sendMailEvent(emailEvent);

      return messageHelper.getVoidResponse(SuccessCode.USER_REGISTERED);
   }

   @Override
   @Transactional
   public VoidResponse confirmRegistrationOtp(ConfirmOtpRequest request) {
      String attemptKey = OtpKeys.OTP_ATTEMPT_PREFIX.getKey() + request.getEmail();
      String totalFailKey = OtpKeys.OTP_TOTAL_FAIL_PREFIX.getKey() + request.getEmail();
      String redisKey = OtpKeys.OTP_PREFIX.getKey() + request.getEmail();

      // 1 dəqiqəlik spam qorunması
      Long attempts = redisService.increment(attemptKey);
      if (attempts != null && attempts == 1) {
         redisService.expire(attemptKey, 1, TimeUnit.MINUTES);
      }
      if (attempts != null && attempts > 3) {
         throw new CustomException(ErrorCodes.TOO_MANY_OTP_ATTEMPTS);
      }

      String storedOtp = redisService.get(redisKey, String.class);

      if (storedOtp == null) {
         throw new CustomException(ErrorCodes.OTP_EXPIRED_OR_NOT_FOUND);
      }

      // A. OTP yanlış olduqda ümumi səhv limitinin yoxlanılması
      if (!storedOtp.equals(request.getOtp())) {
         Long totalFails = redisService.increment(totalFailKey);

         // Ümumi limit açarının ömrünü OTP-nin ömrü ilə eyni (10 dəqiqə) edirik
         if (totalFails != null && totalFails == 1) {
            redisService.expire(totalFailKey, 10, TimeUnit.MINUTES);
         }

         // 5 dəfə yanlış OTP daxil edildikdə OTP tamamilə ləğv edilir
         if (totalFails != null && totalFails >= 5) {
            redisService.delete(redisKey);
            redisService.delete(totalFailKey);
            redisService.delete(attemptKey);
            throw new CustomException(ErrorCodes.OTP_REVOKED_DUE_TO_FAILURES);
         }

         throw new CustomException(ErrorCodes.INVALID_OTP);
      }

      User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() ->
              new CustomException(ErrorCodes.USER_NOT_FOUND_BY_EMAIL, request.getEmail()));

      user.setStatus(Status.ACTIVE);
      userRepository.save(user);

      // Təsdiqləndikdən sonra bütün Redis açarlarının silinməsi
      redisService.delete(redisKey);
      redisService.delete(attemptKey);
      redisService.delete(totalFailKey);

      EmailEvent welcomeEvent = new EmailEvent(user.getEmail(), null, EmailType.WELCOME, mailSecurityToken);
      kafkaProducerService.sendMailEvent(welcomeEvent);

      WalletCreateEvent walletEvent = new WalletCreateEvent(user.getUsername(), user.getEmail());
      kafkaProducerService.sendWalletEvent(String.valueOf(user.getId()), walletEvent);

      return messageHelper.getVoidResponse(SuccessCode.OTP_CONFIRMED);
   }

   @Override
   @Transactional
   public VoidResponse addUser(AddUserRequest request) {
      User user = userMapper.toEntityAdd(request);
      user.setPassword(passwordEncoder.encode(request.getPassword()));
      user.setStatus(Status.ACTIVE);
      Set<Role> roles = new HashSet<>(roleRepository.findAllById(request.getRoleIds()));
      user.setRoles(roles);
      userRepository.save(user);

      // Birbaşa aktiv olaraq əlavə edildiyi üçün cüzdan eventinin göndərilməsi
      WalletCreateEvent walletEvent = new WalletCreateEvent(user.getUsername(), user.getEmail());
      kafkaProducerService.sendWalletEvent(String.valueOf(user.getId()), walletEvent);

      return messageHelper.getVoidResponse(SuccessCode.USER_ADDED);
   }

   @Override
   public VoidResponse updateUser(Long id, UpdateUserRequest request) {
      User user = userRepository.findById(id).orElseThrow(() ->
              new CustomException(ErrorCodes.USER_NOT_FOUND, id));
      userMapper.updateEntity(user, request);
      userRepository.save(user);
      return messageHelper.getVoidResponse(SuccessCode.USER_UPDATED);
   }

   @Override
   public VoidResponse deActiveUser(Long id) {
      User user = userRepository.findById(id).orElseThrow(() ->
              new CustomException(ErrorCodes.USER_NOT_FOUND, id));
      user.setStatus(Status.INACTIVE);
      userRepository.save(user);
      return messageHelper.getVoidResponse(SuccessCode.USER_DEACTIVATED);
   }

   @Override
   public VoidResponse deleteUser(Long id) {
      User user = userRepository.findById(id).orElseThrow(() ->
              new CustomException(ErrorCodes.USER_NOT_FOUND, id));
      user.setStatus(Status.DELETED);
      userRepository.save(user);
      return messageHelper.getVoidResponse(SuccessCode.USER_DELETED);
   }

   @Override
   @Transactional
   public VoidResponse updateUserRoles(Long userId, UpdateUserRoles request) {
      User user = userRepository.findById(userId).orElseThrow(() ->
              new CustomException(ErrorCodes.USER_NOT_FOUND, userId));
      if (request.getAddedRoleIds() != null && !request.getAddedRoleIds().isEmpty()) {
         Set<Role> rolesToAdd = new HashSet<>(roleRepository.findAllById(request.getAddedRoleIds()));
         user.getRoles().addAll(rolesToAdd);
      }
      if (request.getRemovedRoleIds() != null && !request.getRemovedRoleIds().isEmpty()) {
         user.getRoles().removeIf(role -> request.getRemovedRoleIds().contains(role.getId()));
      }
      userRepository.save(user);
      redisService.set(UPDATED_ROLES_PREFIX.getKey() + user.getUsername(), "true", 7, TimeUnit.DAYS);
      return messageHelper.getVoidResponse(SuccessCode.USER_ROLES_UPDATED);
   }

   @Override
   public VoidResponse updateUserPassword(Long userId, UpdatePassword request) {
      User user = userRepository.findById(userId).orElseThrow(() ->
              new CustomException(ErrorCodes.USER_NOT_FOUND, userId));
      user.setPassword(passwordEncoder.encode(request.getNewPassword()));
      userRepository.save(user);
      return messageHelper.getVoidResponse(SuccessCode.USER_PASSWORD_UPDATED);
   }

   @Override
   public VoidResponse forgotPassword(ForgotPasswordRequest request) {
      User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() ->
              new CustomException(ErrorCodes.USER_NOT_FOUND_BY_EMAIL, request.getEmail()));

      String otp = OtpUtil.generate4DigitOtp();
      redisService.set(OtpKeys.OTP_PREFIX.getKey() + user.getEmail(), otp, 10, TimeUnit.MINUTES);

      // Forgot Password OTP-nin Kafka vasitəsilə göndərilməsi
      EmailEvent emailEvent = new EmailEvent(user.getEmail(), otp, EmailType.FORGOT_PASSWORD_OTP, mailSecurityToken);
      kafkaProducerService.sendMailEvent(emailEvent);

      return messageHelper.getVoidResponse(SuccessCode.FORGOT_PASSWORD_OTP_SENT);
   }

   @Override
   public VoidResponse confirmForgotPasswordOtp(ConfirmOtpRequest request) {
      String redisKey = OtpKeys.OTP_PREFIX.getKey() + request.getEmail();
      String storedOtp = redisService.get(redisKey, String.class);

      if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
         throw new CustomException(ErrorCodes.INVALID_OTP);
      }
      // Mark OTP as confirmed for the next step
      redisService.set(OtpKeys.OTP_CONFIRMED_PREFIX.getKey() + request.getEmail(), "true", 5, TimeUnit.MINUTES);
      redisService.delete(redisKey);
      return messageHelper.getVoidResponse(SuccessCode.OTP_CONFIRMED);
   }

   @Override
   public VoidResponse resetPassword(ResetPasswordRequest request) {
      String confirmedKey = OtpKeys.OTP_CONFIRMED_PREFIX.getKey() + request.getEmail();
      String confirmed = redisService.get(confirmedKey, String.class);

      if (confirmed == null || !confirmed.equals("true")) {
         throw new CustomException(ErrorCodes.OTP_NOT_CONFIRMED);
      }

      User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() ->
              new CustomException(ErrorCodes.USER_NOT_FOUND_BY_EMAIL, request.getEmail()));

      user.setPassword(passwordEncoder.encode(request.getNewPassword()));
      userRepository.save(user);

      redisService.delete(confirmedKey);
      return messageHelper.getVoidResponse(SuccessCode.PASSWORD_RESET_SUCCESS);
   }

   @Override
   public Page<UserResponse> searchUsers(SearchUserRequest request) {
      Pageable pageable = PageableCheckUtil.getPageable(request.getPage(), request.getSize());
      return userRepository.findAll(userSpecification.search(request), pageable)
              .map(userMapper::toDto);
   }

   @Override
   public UserResponseAll userById(Long id) {
      User user = userRepository.findUserWithRolesAndPermissionsById(id).orElseThrow(() ->
              new CustomException(ErrorCodes.USER_NOT_FOUND, id));
      return userMapper.toDtoAll(user);
   }

}