package ru.practicum.explorewithme.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Builder
@Getter
@ToString
public class NewEventDto {
    @NotBlank(message = "'annotation' field cannot be empty.")
    private String annotation;

    @NotNull(message = "'category' field cannot be null.")
    @Positive(message = "'category' must be positive.")
    private Long category;

    @NotBlank(message = "'description' field cannot be empty.")
    private String description;

    @NotNull(message = "'eventDate' field cannot be null.")
    private LocalDateTime eventDate;

    @NotNull(message = "'location' field cannot be null.")
    private Location location;

    @NotNull(message = "'paid' field cannot be null.")
    private Boolean paid;

    @NotNull(message = "'participantLimit' field cannot be null.")
    @PositiveOrZero(message = "'participantLimit' must be positive or zero.")
    private Integer participantLimit;

    @NotNull(message = "'requestModeration' field cannot be null.")
    private Boolean requestModeration;

    @NotNull(message = "'status' field cannot be null.")
    private EventStatus status;

    @NotBlank(message = "'title' field cannot be empty.")
    private String title;
}
