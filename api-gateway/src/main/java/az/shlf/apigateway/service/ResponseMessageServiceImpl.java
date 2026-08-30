package az.shlf.apigateway.service;

import az.shlf.apigateway.exception.constants.ErrorCodes;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResponseMessageServiceImpl implements ResponseMessageService {

   private final MessageSource messageSource;

   @Override
   public String getMessage(ErrorCodes errorCode) {
      return messageSource.getMessage(errorCode.getMessage(), null, LocaleContextHolder.getLocale());
   }

   @Override
   public String getMessage(ErrorCodes errorCode, Object... args) {
      return messageSource.getMessage(errorCode.getMessage(), args, LocaleContextHolder.getLocale());
   }
}