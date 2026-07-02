package ru.practicum.explorewithme.common.exception;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {
    private final String entityName;
    private final Long id;

    public NotFoundException(String entityName, Long id) {
        super(entityName + " with id=" + id + " was not found");
        this.entityName = entityName;
        this.id = id;
    }
}
