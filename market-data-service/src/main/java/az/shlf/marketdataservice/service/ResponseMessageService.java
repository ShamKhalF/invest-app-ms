package az.shlf.marketdataservice.service;

import az.shlf.marketdataservice.exception.constants.MessageKeyProvider;

public interface ResponseMessageService {
   String getMessage(MessageKeyProvider provider);

   String getMessage(MessageKeyProvider provider, Object... args);
}