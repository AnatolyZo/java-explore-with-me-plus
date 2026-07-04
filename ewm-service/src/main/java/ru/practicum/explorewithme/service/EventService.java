package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.*;
import ru.practicum.explorewithme.entity.Event;

import java.util.List;

public interface EventService {
    List<EventDto> getEvents(long userId, int from, int size);

    EventDto createEvent(long userId, NewEventDto newEventDto);

    EventDto getEvent(long userId, long eventId);

    EventDto updateEvent(long userId, long eventId, UpdateEventDto updateEventDto);

    List<RequestDto> getRequests(long userId, long eventId);

    List<RequestDto> updateRequestStatuses(long userId, long eventId, UpdateRequestStatusDto update);

    Event getEventById(long eventId, long userId);
}
