package az.shlf.telegramaibot.service;

import az.shlf.telegramaibot.entity.ChatSession;
import az.shlf.telegramaibot.entity.TelegramUser;
import az.shlf.telegramaibot.enums.SessionStatus;
import az.shlf.telegramaibot.enums.UserStatus;
import az.shlf.telegramaibot.repository.ChatSessionRepository;
import az.shlf.telegramaibot.repository.TelegramUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatManagerService {

   private final TelegramUserRepository userRepository;
   private final ChatSessionRepository sessionRepository;

   // 1. İstifadəçinin INACTIVE statusla bazaya yazılması (İlkin qeydiyyat)
   @Transactional
   public TelegramUser createInactiveUser(String username) {
      return userRepository.findByUsernameAndStatus(username, UserStatus.ACTIVE)
              .orElseGet(() -> userRepository.save(
                      TelegramUser.builder()
                              .username(username)
                              .status(UserStatus.INACTIVE)
                              .build()
              ));
   }

   // 2. Telegram linkinə kliklədikdə userin ACTIVE edilməsi
   @Transactional
   public TelegramUser activateUser(String username, Long telegramId) {
      TelegramUser user = userRepository.findByUsernameAndStatus(username, UserStatus.INACTIVE)
              .orElseThrow(() -> new RuntimeException("İnactive istifadəçi tapılmadı və ya artıq aktivdir"));

      user.setTelegramId(telegramId);
      user.setStatus(UserStatus.ACTIVE);
      return userRepository.save(user);
   }


   // 1. Username əsasında userin tapılıb silinməsi
   @Transactional
   public void deleteUserByUsername(String username) {
      TelegramUser user = userRepository.findByUsername(username)
              .orElseThrow(() -> new RuntimeException("İstifadəçi tapılmadı."));

      user.setStatus(UserStatus.DELETED);
      userRepository.save(user);

      // Silinmiş istifadəçinin aktiv sessiyası varsa, dərhal bağlanır
      if (user.getTelegramId() != null) {
         closeSession(user.getTelegramId());
      }
   }

   @Transactional
   public ChatSession openSession(Long telegramId) {
      TelegramUser user = userRepository.findByTelegramIdAndStatus(telegramId, UserStatus.ACTIVE)
              .orElseThrow(() -> new RuntimeException("Aktiv istifadəçi tapılmadı və ya qeydiyyatdan keçməyib"));

      sessionRepository.findFirstByUserTelegramIdAndStatusOrderByIdDesc(telegramId, SessionStatus.OPEN)
              .ifPresent(session -> {
                 session.setStatus(SessionStatus.CLOSED);
                 sessionRepository.save(session);
              });

      ChatSession newSession = ChatSession.builder()
              .user(user)
              .status(SessionStatus.OPEN)
              .build();
      return sessionRepository.save(newSession);
   }

   @Transactional
   public void closeSession(Long telegramId) {
      sessionRepository.findFirstByUserTelegramIdAndStatusOrderByIdDesc(telegramId, SessionStatus.OPEN)
              .ifPresent(session -> {
                 session.setStatus(SessionStatus.CLOSED);
                 sessionRepository.save(session);
              });
   }

   // Bu metod Scheduler tərəfindən hər saat çağırılmalıdır
   @Transactional
   public void closeStaleSessions() {
      LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
      List<ChatSession> staleSessions = sessionRepository.findAllByStatusAndUpdatedAtBefore(SessionStatus.OPEN, oneHourAgo);

      for (ChatSession session : staleSessions) {
         session.setStatus(SessionStatus.CLOSED);
      }
      sessionRepository.saveAll(staleSessions);
   }

   // Bu metodu ChatManagerService daxilinə əlavə edin
   @Transactional(readOnly = true)
   public boolean isActiveUserExists(Long telegramId) {
      return userRepository.findByTelegramIdAndStatus(telegramId, UserStatus.ACTIVE).isPresent();
   }

   public Optional<ChatSession> getActiveSession(Long telegramId) {
      return sessionRepository.findFirstByUserTelegramIdAndStatusOrderByIdDesc(telegramId, SessionStatus.OPEN);
   }




   // 2. TelegramAiBot daxilində statusun təyin edilməsi üçün
   @Transactional(readOnly = true)
   public UserStatus getUserStatus(Long telegramId) {
      return userRepository.findByTelegramId(telegramId)
              .map(TelegramUser::getStatus)
              .orElse(null);
   }


}