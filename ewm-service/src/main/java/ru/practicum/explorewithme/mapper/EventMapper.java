package ru.practicum.explorewithme.mapper;

import ru.practicum.explorewithme.dto.EventDto;
import ru.practicum.explorewithme.dto.Location;
import ru.practicum.explorewithme.dto.NewEventDto;
import ru.practicum.explorewithme.dto.UserShortDto;
import ru.practicum.explorewithme.entity.Category;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.entity.LocationEmbeddable;
import ru.practicum.explorewithme.entity.User;

public class EventMapper {
    public static Event mapToEvent(NewEventDto body,
                                   Category category,
                                   User initiator,
                                   LocationEmbeddable location) {
        return Event.builder()
                .annotation(body.getAnnotation())
                .category(category)
                .description(body.getDescription())
                .eventDate(body.getEventDate())
                .initiator(initiator)
                .location(location)
                .paid(body.getPaid())
                .participantLimit(body.getParticipantLimit())
                .requestModeration(body.getRequestModeration())
                .title(body.getTitle())
                .build();
    }

    public static EventDto mapToEventDto(Event body,
                                         long views) {
        UserShortDto initiator = UserMapper.toUserShortDto(body.getInitiator());
        Location location = LocationMapper.mapToLocation(body.getLocation().getLat(), body.getLocation().getLon());

        return EventDto.builder()
                .id(body.getId())
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
