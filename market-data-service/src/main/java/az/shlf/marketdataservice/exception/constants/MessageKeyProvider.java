package az.shlf.marketdataservice.exception.constants;

import org.springframework.http.HttpStatus;

public interface MessageKeyProvider {

   String getMessage();

   HttpStatus getHttpStatus();
}