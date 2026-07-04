package ru.practicum.explorewithme.mapper;

import ru.practicum.explorewithme.dto.*;
import ru.practicum.explorewithme.entity.Category;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.entity.LocationEmbeddable;
import ru.practicum.explorewithme.entity.User;

import java.time.LocalDateTime;

public class EventMapper {
    public static Event mapToEvent(NewEventDto body,
                                   Category category,
                                   User initiator,
                                   LocationEmbeddable location) {
        return Event.builder()
                .annotation(body.getAnnotation())
                .category(category)
                .confirmedRequests(0)
                .createdOn(LocalDateTime.now())
                .description(body.getDescription())
                .eventDate(body.getEventDate())
                .initiator(initiator)
                .location(location)
                .paid(body.getPaid())
                .participantLimit(body.getParticipantLimit())
                .publishedOn(null)
                .requestModeration(body.getRequestModeration())
                .status(EventStatus.PENDING)
                .title(body.getTitle())
                .build();
    }

    public static EventDto mapToEventDto(Event body,
                                         long views) {
        UserShortDto initiator = UserMapper.toUserShortDto(body.getInitiator());
        Location location = LocationMapper.mapToLocation(body.getLocation().getLat(), body.getLocation().getLon());

        return EventDto.builder()
                .annotation(body.getAnnotation())
                .category(body.getCategory().getId())
                .confirmedRequests(body.getConfirmedRequests())
                .createdOn(body.getCreatedOn())
                .description(body.getDescription())
                .eventDate(body.getEventDate())
                .initiator(initiator)
                .location(location)
                .paid(body.isPaid())
                .participantLimit(body.getParticipantLimit())
                .publishedOn(body.getPublishedOn())
                .requestModeration(body.isRequestModeration())
                .status(body.getStatus())
                .title(body.getTitle())
                .views(views)
                .build();
    }
}
