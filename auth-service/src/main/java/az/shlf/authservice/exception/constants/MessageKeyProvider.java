package az.shlf.authservice.exception.constants;

import org.springframework.http.HttpStatus;

public interface MessageKeyProvider {

   String getMessage();

   HttpStatus getHttpStatus();
}