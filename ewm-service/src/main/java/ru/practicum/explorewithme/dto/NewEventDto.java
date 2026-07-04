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
public class NewEventDto {
    @NotBlank
    private String annotation;

    @NotNull
    private Long category;

    @NotBlank
    private String description;

    @NotNull
    @DateIsNotEarly
    private LocalDateTime eventDate;

    @NotNull
    private Location location;

    @NotNull
    private Boolean paid;

    @NotNull
    @Positive
    private Integer participantLimit;

    @NotNull
    private Boolean requestModeration;
    private EventStatus status;

    @NotBlank
    private String title;
}
