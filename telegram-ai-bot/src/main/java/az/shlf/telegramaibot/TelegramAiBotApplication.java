package az.shlf.telegramaibot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@SpringBootApplication
@EnableScheduling
public class TelegramAiBotApplication {

   public static void main(String[] args) {
      SpringApplication.run(TelegramAiBotApplication.class, args);
   }


}
