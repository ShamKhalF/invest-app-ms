package az.shlf.telegramaibot.repository;

import az.shlf.telegramaibot.entity.ChatSession;
import az.shlf.telegramaibot.enums.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
   Optional<ChatSession> findFirstByUserTelegramIdAndStatusOrderByIdDesc(Long telegramId, SessionStatus status);

   // 1 saatdan çox hərəkətsiz qalan sessiyaları tapmaq üçün
   List<ChatSession> findAllByStatusAndUpdatedAtBefore(SessionStatus status, LocalDateTime time);

}