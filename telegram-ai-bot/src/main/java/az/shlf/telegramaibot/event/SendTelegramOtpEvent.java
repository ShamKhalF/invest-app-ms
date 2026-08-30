package az.shlf.telegramaibot.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SendTelegramOtpEvent extends ApplicationEvent {
   private final Long chatId;
   private final String otpCode;

   public SendTelegramOtpEvent(Object source, Long chatId, String otpCode) {
      super(source);
      this.chatId = chatId;
      this.otpCode = otpCode;
   }
}