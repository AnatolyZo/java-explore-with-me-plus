package ru.practicum.explorewithme.mapper;


import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.explorewithme.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.dto.UserShortDto;
import ru.practicum.explorewithme.entity.Request;

@AllArgsConstructor
@Component
public class ParticipationRequestMapper {

    public ParticipationRequestDto toParticipationRequestDto(Request request) {

        UserShortDto requesterDto;
        if (request.getRequester() != null) {
            requesterDto = new UserShortDto(
                    request.getRequester().getId(),
                    request.getRequester().getName()
            );
        } else {
            requesterDto = null;
        }

        return new ParticipationRequestDto(
                request.getId(),
                request.getCreated(),
                request.getEvent() != null ? request.getEvent().getId() : null,
                request.getRequester() != null ? request.getRequester().getId() : null,
                requesterDto,
                request.getEvent() != null ? request.getEvent().getId() : null,
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
