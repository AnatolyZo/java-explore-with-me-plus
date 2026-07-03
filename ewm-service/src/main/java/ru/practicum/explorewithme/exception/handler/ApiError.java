package ru.practicum.explorewithme.exception.handler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@ToString
public class ApiError {
    private String message;
    private String reason;
    private String status;
    private LocalDateTime timestamp;
}
