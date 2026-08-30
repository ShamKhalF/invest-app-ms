package az.shlf.apigateway.service;

import az.shlf.apigateway.exception.constants.ErrorCodes;

public interface ResponseMessageService {
   String getMessage(ErrorCodes errorCode);

   String getMessage(ErrorCodes errorCode, Object... args);
}