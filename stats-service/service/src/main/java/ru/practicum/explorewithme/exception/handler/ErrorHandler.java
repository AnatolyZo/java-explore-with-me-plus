package ru.practicum.explorewithme.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@SuppressWarnings("unused")
public class ErrorHandler {
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> unexpected(Throwable e) {
        return createResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "SERVICE: unexpected error",
                e.getMessage());
    }

    private ResponseEntity<ErrorResponse> createResponse(HttpStatus status, String reason, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                status.toString(),
                reason,
                message,
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> methodArgumentNotValid(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getFieldError();
        if (fieldError == null) {
            return createResponse(HttpStatus.BAD_REQUEST, "incorrect field value");
        }
        return createResponse(
                HttpStatus.BAD_REQUEST,
                "incorrect field value",
                String.format(
                        "value of field '%s'=%s is incorrect, cause: %s",
                        fieldError.getField(),
                        fieldError.getRejectedValue(),
                        fieldError.getDefaultMessage()));
    }

    private ResponseEntity<ErrorResponse> createResponse(HttpStatus status, String reason) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                status.toString(),
                reason,
                "",
                LocalDateTime.now()
        ));
    }
}
