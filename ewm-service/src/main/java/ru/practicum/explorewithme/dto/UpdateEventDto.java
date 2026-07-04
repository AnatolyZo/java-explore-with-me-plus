package ru.practicum.explorewithme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import ru.practicum.explorewithme.validation.DateIsNotEarly;

import java.time.LocalDateTime;

@Builder
@Getter
@ToString
public class UpdateEventDto {
    private String annotation;

    @Positive
    private Long category;

    private String description;

    @DateIsNotEarly
    private LocalDateTime eventDate;

    private Location location;

    private Boolean paid;

    @Positive
    private Integer participantLimit;

    private Boolean requestModeration;

    private EventUpdateAction status;

    private String title;
}
