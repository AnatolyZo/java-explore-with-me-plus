package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.EventDto;
import ru.practicum.explorewithme.dto.NewEventDto;
import ru.practicum.explorewithme.dto.RequestDto;
import ru.practicum.explorewithme.dto.UpdateRequestStatusDto;
import ru.practicum.explorewithme.entity.Event;

import java.util.List;

public interface EventService {
    List<EventDto> getEvents(long userId, int from, int size);

    EventDto createEvent(long userId, NewEventDto newEventDto);

    EventDto getEvent(long userId, long eventId);

    EventDto updateEvent(long userId, long eventId, NewEventDto newEventDto);

    List<RequestDto> getRequests(long userId, long eventId);

    List<RequestDto> updateRequestStatuses(long userId, long eventId, UpdateRequestStatusDto update);

    Event getEventById(long eventId, long userId);
}
