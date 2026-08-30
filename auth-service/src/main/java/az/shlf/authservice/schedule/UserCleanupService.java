package az.shlf.authservice.schedule;

import az.shlf.authservice.contants.entity.Status;
import az.shlf.authservice.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class UserCleanupService {

   private final UserRepository userRepository;

   public UserCleanupService(UserRepository userRepository) {
      this.userRepository = userRepository;
   }

   @Scheduled(cron = "0 0 * * * ?")
   @Transactional
   public void cleanUpUnconfirmedUsers() {
      LocalDateTime thresholdDate = LocalDateTime.now().minusMinutes(10);
      userRepository.deleteByStatusAndCreatedAtBefore(Status.NOT_CONFIRMED, thresholdDate);
   }

}