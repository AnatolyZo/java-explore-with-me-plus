package ru.practicum.explorewithme.common.exception.handler;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.explorewithme.common.exception.ConflictException;
import ru.practicum.explorewithme.common.exception.NotFoundException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException exception) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "The required object was not found.",
                exception.getMessage()
        );
    }

    @ExceptionHandler({ConflictException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiError> handleConflict(RuntimeException exception) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Integrity constraint has been violated.",
                exception.getMessage()
        );
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(Exception exception) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Incorrectly made request.",
                exception.getMessage()
        );
    }

    private ResponseEntity<ApiError> buildErrorResponse(HttpStatus status, String reason, String message) {
        ApiError error = new ApiError(message, reason, status.toString(), LocalDateTime.now());
        return ResponseEntity.status(status).body(error);
    }
}
