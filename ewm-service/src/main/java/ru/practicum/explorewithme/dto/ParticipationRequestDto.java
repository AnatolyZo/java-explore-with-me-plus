package ru.practicum.explorewithme.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {
    private Long id;
    private LocalDateTime created;
    private Long eventId;
    private Long requesterId;
    private EventShortDto event;
    private UserShortDto requester;
    private RequestStatus status;
}
