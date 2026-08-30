package az.shlf.telegramaibot.util;

import az.shlf.telegramaibot.dto.response.CommonResponse;
import az.shlf.telegramaibot.exception.constants.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageHelper {

   private final MessageSource messageSource;

   public ResponseEntity<CommonResponse<Void>> getVoidResponse(SuccessCode successCode) {
      String message = messageSource.getMessage(
              successCode.getMessage(),
              null,
              LocaleContextHolder.getLocale()
      );

      CommonResponse<Void> response = CommonResponse.<Void>builder()
              .code(successCode.name())
              .message(message)
              .build();

      return new ResponseEntity<>(response, successCode.getHttpStatus());
   }

   public <T> ResponseEntity<CommonResponse<T>> getSuccessResponse(SuccessCode successCode, T data) {
      String message = messageSource.getMessage(
              successCode.getMessage(),
              null,
              LocaleContextHolder.getLocale()
      );

      CommonResponse<T> response = CommonResponse.<T>builder()
              .code(successCode.name())
              .message(message)
              .data(data)
              .build();

      return new ResponseEntity<>(response, successCode.getHttpStatus());
   }
}