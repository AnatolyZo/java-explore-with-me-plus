package ru.practicum.explorewithme.exception;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {
    private final String objectType;
    private final long id;

    public NotFoundException(String objectType, long id) {
        super(String.format("%s with id=%d not exists", objectType, id));
        this.objectType = objectType;
        this.id = id;
    }
}
