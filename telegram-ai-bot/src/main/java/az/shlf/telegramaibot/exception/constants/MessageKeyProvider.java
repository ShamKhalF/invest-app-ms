package az.shlf.telegramaibot.exception.constants;

import org.springframework.http.HttpStatus;

public interface MessageKeyProvider {

   String getMessage();

   HttpStatus getHttpStatus();
}