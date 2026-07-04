package ru.practicum.explorewithme.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Builder
@Getter
@ToString
public class UpdateEventDto {
    private String annotation;

    @Positive(message = "'category' must be positive.")
    private Long category;

    private String description;

    private LocalDateTime eventDate;

    private Location location;

    private Boolean paid;

    @PositiveOrZero(message = "'participantLimit' must be positive or zero.")
    private Integer participantLimit;

    private Boolean requestModeration;

    private EventUpdateAction status;

    private String title;
}
