package ru.practicum.explorewithme.common.exception.handler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ApiError {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<String> errors;
    private final String message;
    private final String reason;
    private final String status;
    private final String timestamp;

    public static ApiError of(HttpStatus status, String reason, String message) {
        return ApiError.builder()
                .errors(List.of())
                .message(message)
                .reason(reason)
                .status(status.toString())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }
}
