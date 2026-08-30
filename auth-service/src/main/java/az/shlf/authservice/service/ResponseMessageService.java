package az.shlf.authservice.service;

import az.shlf.authservice.exception.constants.MessageKeyProvider;

public interface ResponseMessageService {
   String getMessage(MessageKeyProvider provider);

   String getMessage(MessageKeyProvider provider, Object... args);
}