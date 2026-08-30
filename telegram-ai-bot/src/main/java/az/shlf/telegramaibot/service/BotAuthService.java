package az.shlf.telegramaibot.service;

import az.shlf.telegramaibot.dto.response.BotLinkResponseDto;
import az.shlf.telegramaibot.dto.response.CommonResponse;
import az.shlf.telegramaibot.entity.TelegramUser;
import az.shlf.telegramaibot.event.SendTelegramOtpEvent;
import az.shlf.telegramaibot.exception.constants.SuccessCode;
import az.shlf.telegramaibot.repository.TelegramUserRepository;
import az.shlf.telegramaibot.util.MessageHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class BotAuthService {

   private final ChatManagerService chatManagerService;
   private final TelegramUserRepository userRepository;
   private final StringRedisTemplate redisTemplate;
   private final MessageHelper messageHelper;
   private final ApplicationEventPublisher eventPublisher;

   @Value("${telegrambots.bot-username}")
   private String botUsername;

   public ResponseEntity<CommonResponse<BotLinkResponseDto>> generateBotLink(String username) {
      chatManagerService.createInactiveUser(username);
      String botUrl = String.format("https://t.me/%s?start=%s", botUsername, username);

      BotLinkResponseDto data = BotLinkResponseDto.builder()
              .botUrl(botUrl)
              .build();

      return messageHelper.getSuccessResponse(SuccessCode.TELEGRAM_LINK_GENERATED, data);
   }

   public ResponseEntity<CommonResponse<Void>> initiateDeleteTelegramUser(String username) {
      TelegramUser user = userRepository.findByUsername(username)
              .orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı."));

      if (user.getTelegramId() == null) {
         throw new RuntimeException("Bu istifadəçinin aktiv Telegram hesabı yoxdur.");
      }

      String otp = String.format("%06d", new Random().nextInt(999999));
      String redisKey = "DELETE_TELEGRAM_USER_OTP:" + username;

      redisTemplate.opsForValue().set(redisKey, otp, 10, TimeUnit.MINUTES);

      // TODO blokunun əvəzinə, məlumatı Event vasitəsilə bota göndəririk
      eventPublisher.publishEvent(new SendTelegramOtpEvent(this, user.getTelegramId(), otp));

      return messageHelper.getVoidResponse(SuccessCode.TELEGRAM_DELETE_OTP_SENT);
   }

   public ResponseEntity<CommonResponse<Void>> verifyOtpAndDeleteUser(String username, String otp) {
      String redisKey = "DELETE_TELEGRAM_USER_OTP:" + username;
      String cachedOtp = redisTemplate.opsForValue().get(redisKey);

      if (cachedOtp == null) {
         throw new RuntimeException("OTP kodunun vaxtı bitib və ya mövcud deyil.");
      }

      if (!cachedOtp.equals(otp)) {
         throw new RuntimeException("Daxil edilən OTP kodu yanlışdır.");
      }

      chatManagerService.deleteUserByUsername(username);
      redisTemplate.delete(redisKey);

      return messageHelper.getVoidResponse(SuccessCode.TELEGRAM_USER_DELETED);
   }
}