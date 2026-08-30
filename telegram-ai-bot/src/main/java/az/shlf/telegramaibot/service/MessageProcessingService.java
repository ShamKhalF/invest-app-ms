package az.shlf.telegramaibot.service;

import az.shlf.telegramaibot.entity.ChatSession;
import az.shlf.telegramaibot.entity.Message;
import az.shlf.telegramaibot.enums.SenderType;
import az.shlf.telegramaibot.repository.ChatSessionRepository;
import az.shlf.telegramaibot.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageProcessingService {

   private final MessageRepository messageRepository;
   private final ChatSessionRepository chatSessionRepository;

   @Transactional
   public void saveMessage(ChatSession session, SenderType senderType, String text) {
      Message message = Message.builder()
              .session(session)
              .senderType(senderType)
              .text(text)
              .build();
      messageRepository.save(message);

      // Sessiyanın son aktivlik tarixini yeniləyirik ki, stalesession (köhnəlmiş sessiya) məntiqi düzgün işləsin
      session.setUpdatedAt(LocalDateTime.now());
      chatSessionRepository.save(session);
   }

   @Transactional(readOnly = true)
   public List<Message> getSessionHistory(Long sessionId) {
      return messageRepository.findAllBySessionIdOrderByCreatedAtAsc(sessionId);
   }
   
}