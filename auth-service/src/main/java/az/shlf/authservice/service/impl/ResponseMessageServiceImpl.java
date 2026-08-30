package az.shlf.authservice.service.impl;

import az.shlf.authservice.exception.constants.MessageKeyProvider;
import az.shlf.authservice.service.ResponseMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResponseMessageServiceImpl implements ResponseMessageService {

   private final MessageSource messageSource;

   @Override
   public String getMessage(MessageKeyProvider provider) {
      return messageSource.getMessage(provider.getMessage(), null, LocaleContextHolder.getLocale());
   }

   @Override
   public String getMessage(MessageKeyProvider provider, Object... args) {
      return messageSource.getMessage(provider.getMessage(), args, LocaleContextHolder.getLocale());
   }

}
