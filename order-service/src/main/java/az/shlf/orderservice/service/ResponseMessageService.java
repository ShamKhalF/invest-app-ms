package az.shlf.orderservice.service;

import az.shlf.orderservice.exception.constants.MessageKeyProvider;

public interface ResponseMessageService {
   String getMessage(MessageKeyProvider provider);

   String getMessage(MessageKeyProvider provider, Object... args);
}