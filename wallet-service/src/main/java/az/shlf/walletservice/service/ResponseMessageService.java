package az.shlf.walletservice.service;

import az.shlf.walletservice.exception.constants.MessageKeyProvider;

public interface ResponseMessageService {
   String getMessage(MessageKeyProvider provider);

   String getMessage(MessageKeyProvider provider, Object... args);
}