package az.shlf.authservice.util;

import az.shlf.authservice.dto.response.VoidResponse;
import az.shlf.authservice.exception.constants.MessageKeyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResponseMessageHelper {

   private final MessageSource messageSource;

   public String getMessage(MessageKeyProvider keyProvider, Object... args) {
      return messageSource.getMessage(keyProvider.getMessage(), args, LocaleContextHolder.getLocale());
   }

   public VoidResponse getVoidResponse(MessageKeyProvider keyProvider, Object... args) {
      return VoidResponse.builder()
              .code(keyProvider.getHttpStatus().value())
              .message(getMessage(keyProvider, args))
              .build();
   }
}
