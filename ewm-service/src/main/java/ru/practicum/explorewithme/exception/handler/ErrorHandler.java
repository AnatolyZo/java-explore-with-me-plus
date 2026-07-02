package ru.practicum.explorewithme.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.explorewithme.exception.DuplicatedDataException;
import ru.practicum.explorewithme.exception.NotEmptyCategoryException;
import ru.practicum.explorewithme.exception.NotFoundException;

import java.time.LocalDateTime;

@RestControllerAdvice
@SuppressWarnings("unused")
public class ErrorHandler {
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> unexpected(Throwable e) {
        return createResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "unexpected error",
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
            return createResponse(HttpStatus.BAD_REQUEST, "incorrect field value", "");
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

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> duplicatedData(DuplicatedDataException e) {
        return createResponse(HttpStatus.CONFLICT, "duplicated data", e.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> notFound(NotFoundException e) {
        return createResponse(HttpStatus.NOT_FOUND, "required object was not found", e.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> notEmptyCategory(NotEmptyCategoryException e) {
        return createResponse(HttpStatus.CONFLICT, "trying delete category with events", e.getMessage());
    }
}
