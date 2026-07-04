package ru.practicum.explorewithme.exception;

public class UnavailableUpdateException extends RuntimeException {
    public UnavailableUpdateException(long eventId) {
        super(String.format("Event with id %d update is unavailable", eventId));
    }
}
