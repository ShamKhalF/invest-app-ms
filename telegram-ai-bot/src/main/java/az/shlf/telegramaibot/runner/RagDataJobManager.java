package az.shlf.telegramaibot.runner;

import az.shlf.telegramaibot.service.RagIngestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class RagDataJobManager implements CommandLineRunner {

   private final RagIngestionService ragIngestionService;

   // Tətbiq işə düşdüyü an icra edilir (Son 1 ayın xəbərləri və statik datalar)
   @Override
   public void run(String... args) {
      log.info("[STARTUP] RAG bazasının ilkin doldurulması prosesi başlayır...");
      ragIngestionService.processAndSaveAllData();
   }

   // Hər 30 dəqiqədən bir avtomatik icra edilir
   @Scheduled(cron = "0 0/30 * * * *")
   public void scheduledDataSync() {
      log.info("[CRON] RAG bazasının periodik sinxronizasiyası başlayır...");
      ragIngestionService.processAndSaveAllData();
   }
}