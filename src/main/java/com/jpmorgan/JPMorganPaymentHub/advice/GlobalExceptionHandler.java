package com.jpmorgan.JPMorganPaymentHub.advice;


import com.jpmorgan.JPMorganPaymentHub.constant.AppConstants;
import com.jpmorgan.JPMorganPaymentHub.enums.CustomErrorCode;
import com.jpmorgan.JPMorganPaymentHub.exception.BadRequestException;
import com.jpmorgan.JPMorganPaymentHub.exception.ResourceNotFoundException;
import com.jpmorgan.JPMorganPaymentHub.model.Error;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.handler.ResponseStatusExceptionHandler;
import reactor.core.publisher.Mono;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseStatusExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Error>> handleGenericException(Exception ex, ServerWebExchange exchange) {
        log.error("handleGenericException invoked with exception: {}", ex.getMessage());
        com.jpmorgan.JPMorganPaymentHub.model.Error error = new com.jpmorgan.JPMorganPaymentHub.model.Error(ex.getMessage(), CustomErrorCode.ERR_4211.getCode(), CustomErrorCode.ERR_4211.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<Error>> handleIllegalArgumentException(IllegalArgumentException ex, ServerWebExchange exchange) {
        log.error("handleIllegalArgumentException invoked with exception: {}", ex.getMessage());
        Error error = new Error(ex.getMessage(), CustomErrorCode.ERR_4212.getCode(), CustomErrorCode.ERR_4212.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<Error>> handleResourceNotFoundException(ResourceNotFoundException ex, ServerWebExchange exchange) {
        log.error("handleResourceNotFoundException invoked with exception: {}", ex.getMessage());
        Error error = new Error(ex.getMessage(), CustomErrorCode.ERR_4253.getCode(), CustomErrorCode.ERR_4253.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }

    @ExceptionHandler(BadRequestException.class)
    public Mono<ResponseEntity<Error>> handleBadRequestException(BadRequestException ex, ServerWebExchange exchange) {
        log.error("handleBadRequestException invoked with exception: {}", ex.getMessage());
        Error error = new Error(ex.getMessage(), CustomErrorCode.ERR_4252.getCode(), CustomErrorCode.ERR_4252.getMessage());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

}