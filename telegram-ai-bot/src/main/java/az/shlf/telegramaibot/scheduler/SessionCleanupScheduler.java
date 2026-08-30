package az.shlf.telegramaibot.scheduler;

import az.shlf.telegramaibot.service.ChatManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionCleanupScheduler {

   private final ChatManagerService chatManagerService;

   @Scheduled(cron = "0 */10 * * * *")
   public void scheduleStaleSessionCleanup() {
      log.info("Köhnəlmiş sessiyaların təmizlənməsi prosesi işə düşdü.");
      try {
         chatManagerService.closeStaleSessions();
      } catch (Exception e) {
         log.error("Sessiyaların təmizlənməsi zamanı xəta baş verdi: {}", e.getMessage());
      }
   }
}