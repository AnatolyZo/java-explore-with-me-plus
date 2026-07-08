package ru.practicum.explorewithme.mapper;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.dto.EventShortDto;
import ru.practicum.explorewithme.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.dto.UserShortDto;
import ru.practicum.explorewithme.entity.Request;

@AllArgsConstructor
@Component
public class ParticipationRequestMapper {

    public ParticipationRequestDto toParticipationRequestDto(Request request) {

        UserShortDto userDto;
        if (request.getRequester() != null) {
            userDto = new UserShortDto(
                    request.getRequester().getId(),
                    request.getRequester().getName()
            );
        } else {
            userDto = null;
        }

        EventShortDto eventDto;
        if (request.getEvent() != null) {
            eventDto = new EventShortDto(
                    request.getEvent().getId(),
                    request.getEvent().getDescription()
            );
        } else {
            eventDto = null;
        }

        return new ParticipationRequestDto(
                request.getId(),
                request.getCreated(),
                request.getEvent() != null ? request.getEvent().getId() : null,
                request.getRequester() != null ? request.getRequester().getId() : null,
                eventDto,
                userDto,
                request.getStatus()
        );
    }


    public Request toRequest(ParticipationRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Request request = new Request();
        request.setId(dto.getId());
        request.setCreated(dto.getCreated());
        request.setStatus(dto.getStatus());
        return request;
    }

}
