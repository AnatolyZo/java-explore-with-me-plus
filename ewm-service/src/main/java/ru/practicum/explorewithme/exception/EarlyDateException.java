package ru.practicum.explorewithme.exception;

import java.time.LocalDateTime;

public class EarlyDateException extends RuntimeException {
    public EarlyDateException(LocalDateTime date) {
        super(String.format("Время начала события должно начинаться не ранее, чем через два часа. Ваше время %s", date));
    }
}
