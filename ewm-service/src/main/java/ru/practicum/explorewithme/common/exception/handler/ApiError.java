package ru.practicum.explorewithme.common.exception.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ApiError {
    private final String message;
    private final String reason;
    private final String status;
    private final LocalDateTime timestamp;
}
