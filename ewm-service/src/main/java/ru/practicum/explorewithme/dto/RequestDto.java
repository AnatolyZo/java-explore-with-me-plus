package ru.practicum.explorewithme.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Builder
@Getter
@ToString
public class RequestDto {
    private long id;
    private LocalDateTime created;
    private long eventId;
    private long requesterId;
    private RequestStatus status;
}
