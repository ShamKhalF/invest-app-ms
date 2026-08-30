package az.shlf.orderservice.exception.constants;

import org.springframework.http.HttpStatus;

public interface MessageKeyProvider {

   String getMessage();

   HttpStatus getHttpStatus();
}