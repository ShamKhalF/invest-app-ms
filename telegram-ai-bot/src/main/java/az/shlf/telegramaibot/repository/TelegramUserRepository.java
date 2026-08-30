package az.shlf.telegramaibot.repository;

import az.shlf.telegramaibot.entity.TelegramUser;
import az.shlf.telegramaibot.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {
   Optional<TelegramUser> findByTelegramId(Long telegramId);

   Optional<TelegramUser> findByTelegramIdAndStatus(Long telegramId, UserStatus status);
   Optional<TelegramUser> findByUsernameAndStatus(String username, UserStatus status);


   Optional<TelegramUser> findByUsername(String username);

}