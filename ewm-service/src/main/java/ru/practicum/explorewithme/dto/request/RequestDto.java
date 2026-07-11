package ru.practicum.explorewithme.dto.request;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import ru.practicum.explorewithme.dto.event.EventDto;
import ru.practicum.explorewithme.dto.event.EventShortDto;
import ru.practicum.explorewithme.dto.user.UserShortDto;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.entity.User;

import java.time.LocalDateTime;

@Builder
@Getter
@ToString
public class RequestDto {
    private long id;
    private LocalDateTime created;
    private Long event;
    private UserShortDto requester;
    private RequestStatus status;
}
