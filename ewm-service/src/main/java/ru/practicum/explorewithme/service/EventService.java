package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.*;
import ru.practicum.explorewithme.entity.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventDto> getEvents(long userId, int from, int size);

    EventDto createEvent(long userId, NewEventDto newEventDto);

    EventDto getEvent(long userId, long eventId);

    EventDto updateEvent(long userId, long eventId, UpdateEventDto updateEventDto);

    List<RequestDto> getRequests(long userId, long eventId);

    ChangedRequestStatusesDto updateRequestStatuses(long userId, long eventId, UpdateRequestStatusDto update);

    Event getEventById(long eventId, long userId);

    List<EventShortDto> getPublishedEvents(String text,
                                            List<Long> categories,
                                            Boolean paid,
                                            LocalDateTime rangeStart,
                                            LocalDateTime rangeEnd,
                                            boolean onlyAvailable,
                                            PublicEventSort sort,
                                            int from,
                                            int size,
                                            String ip,
                                            String uri);

    EventFullDto getPublishedEvent(long eventId, String ip, String uri);
}
