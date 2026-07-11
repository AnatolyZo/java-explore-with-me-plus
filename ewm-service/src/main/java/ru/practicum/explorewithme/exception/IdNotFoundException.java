package ru.practicum.explorewithme.exception;

import lombok.Getter;

@Getter
public class IdNotFoundException extends RuntimeException {
    private final long id;

    public IdNotFoundException(long id) {
        super(String.format("entity with id=%d not exists", id));
        this.id = id;
    }
}
