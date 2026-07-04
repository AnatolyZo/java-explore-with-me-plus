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
import ru.practicum.explorewithme.validation.DateIsNotEarly;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@RestControllerAdvice
@SuppressWarnings("unused")
public class ErrorHandler {
    @ExceptionHandler
    public ResponseEntity<ApiError> unexpected(Throwable e) {
        return createErrorResponse(
                "unexpected error",
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private ResponseEntity<ApiError> createErrorResponse(String message, String reason, HttpStatus status) {
        return ResponseEntity.status(status).body(
                new ApiError(
                        message,
                        reason,
                        status.toString(),
                        LocalDateTime.now()
                )
        );
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> methodArgumentNotValid(MethodArgumentNotValidException e) throws NoSuchMethodException {
        FieldError fieldError = e.getFieldError();
        if (fieldError == null) {
            return createErrorResponse("incorrect field value", "", HttpStatus.BAD_REQUEST);
        }

        //Получение значения сообщения по умолчанию аннотации DateIsNotEarly
        Method messageMethod = DateIsNotEarly.class.getMethod("message");
        String defaultMessage = (String) messageMethod.getDefaultValue();

        if (defaultMessage.equals(fieldError.getDefaultMessage())) {
            return createErrorResponse(
                    String.format("Field: %s. Error: %s. Value: %s",
                            fieldError.getField(),
                            fieldError.getDefaultMessage(),
                            fieldError.getRejectedValue()),
                    "For the requested operation the conditions are not met.",
                    HttpStatus.FORBIDDEN);
        } else {
            return createErrorResponse(
                    "incorrect field value",
                    String.format(
                            "value of field '%s'=%s is incorrect, cause: %s",
                            fieldError.getField(),
                            fieldError.getRejectedValue(),
                            fieldError.getDefaultMessage()),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> duplicatedData(DuplicatedDataException e) {
        return createErrorResponse("duplicated data", e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> notFound(NotFoundException e) {
        return createErrorResponse("required object was not found", e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<ApiError> notEmptyCategory(NotEmptyCategoryException e) {
        return createErrorResponse("trying delete category with events", e.getMessage(), HttpStatus.CONFLICT);
    }
}
