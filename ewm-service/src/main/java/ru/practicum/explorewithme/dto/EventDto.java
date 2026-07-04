package ru.practicum.explorewithme.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Builder
@Getter
@ToString
public class EventDto {
    private long id;
    private String annotation;
    private long category;
    private int confirmedRequests;
    private LocalDateTime createdOn;
    private String description;
    private LocalDateTime eventDate;
    private UserShortDto initiator;
    private Location location;
    private boolean paid;
    private int participantLimit;
    private LocalDateTime publishedOn;
    private boolean requestModeration;
    private EventStatus status;
    private String title;
    private long views;
}
