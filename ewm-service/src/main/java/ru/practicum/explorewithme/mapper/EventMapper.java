package ru.practicum.explorewithme.mapper;

import ru.practicum.explorewithme.dto.*;
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
                .paid(body.isPaid())
                .participantLimit(body.getParticipantLimit())
                .requestModeration(body.isRequestModeration())
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
                .category(CategoryMapper.toCategoryDto(body.getCategory()))
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

    public static EventShortDto mapToEventShortDto(Event body,
                                                   long views) {
        UserShortDto initiator = UserMapper.toUserShortDto(body.getInitiator());
        CategoryDto category = CategoryMapper.toCategoryDto(body.getCategory());

        return EventShortDto.builder()
                .id(body.getId())
                .annotation(body.getAnnotation())
                .category(category)
                .confirmedRequests(body.getConfirmedRequests())
                .eventDate(body.getEventDate())
                .initiator(initiator)
                .paid(body.isPaid())
                .title(body.getTitle())
                .views(views)
                .build();
    }
}
