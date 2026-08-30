package az.shlf.telegramaibot.repository;

import az.shlf.telegramaibot.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
   // Söhbət tarixçəsini ardıcıllıqla çəkmək üçün
   List<Message> findAllBySessionIdOrderByCreatedAtAsc(Long sessionId);
}