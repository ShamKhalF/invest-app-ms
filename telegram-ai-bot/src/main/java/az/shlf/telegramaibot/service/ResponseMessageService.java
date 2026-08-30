package az.shlf.telegramaibot.service;

import az.shlf.telegramaibot.exception.constants.MessageKeyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResponseMessageService {

   private final MessageSource messageSource;

   public String getMessage(MessageKeyProvider provider) {
      return messageSource.getMessage(provider.getMessage(), null, LocaleContextHolder.getLocale());
   }

   public String getMessage(MessageKeyProvider provider, Object... args) {
      return messageSource.getMessage(provider.getMessage(), args, LocaleContextHolder.getLocale());
   }

}

