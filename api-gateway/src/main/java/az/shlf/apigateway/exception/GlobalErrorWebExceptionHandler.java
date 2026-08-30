package az.shlf.apigateway.exception;

import az.shlf.apigateway.exception.dto.ExceptionResponse;
import az.shlf.apigateway.exception.constants.ErrorCodes;
import az.shlf.apigateway.service.ResponseMessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@Order(-1)
public class GlobalErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;
    private final ResponseMessageService responseMessageService;

    public GlobalErrorWebExceptionHandler(ObjectMapper objectMapper, ResponseMessageService responseMessageService) {
        this.objectMapper = objectMapper;
        this.responseMessageService = responseMessageService;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        var response = exchange.getResponse();

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = responseMessageService.getMessage(ErrorCodes.INTERNAL_SERVER_ERROR);
        String code = ErrorCodes.INTERNAL_SERVER_ERROR.name();

        if (ex instanceof ResponseStatusException responseStatusException) {
            status = HttpStatus.valueOf(responseStatusException.getStatusCode().value());
            code = status.name();
            message = responseStatusException.getReason(); // Bu hal üçün xüsusi tərcümələr də əlavə edilə bilər
        }

        response.setStatusCode(status);

        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .status(status.value())
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now().toString())
                .path(exchange.getRequest().getURI().getPath())
                .build();

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(exceptionResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

}