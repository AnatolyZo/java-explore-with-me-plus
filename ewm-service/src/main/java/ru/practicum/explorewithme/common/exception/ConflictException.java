package ru.practicum.explorewithme.common.exception;

import lombok.Getter;

@Getter
public class ConflictException extends RuntimeException {
    private final String entityName;
    private final String fieldName;
    private final String fieldValue;

    public ConflictException(String entityName, String fieldName, String fieldValue) {
        super(entityName + " with " + fieldName + "=" + fieldValue + " already exists");
        this.entityName = entityName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }
}
