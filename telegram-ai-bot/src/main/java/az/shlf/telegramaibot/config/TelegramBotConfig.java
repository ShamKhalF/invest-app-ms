package az.shlf.telegramaibot.config;

import az.shlf.telegramaibot.bot.TelegramAiBot;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramBotConfig {

   @Bean
   public TelegramBotsApi telegramBotsApi(TelegramAiBot telegramAiBot) throws TelegramApiException {
      TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
      api.registerBot(telegramAiBot);
      return api;
   }

}