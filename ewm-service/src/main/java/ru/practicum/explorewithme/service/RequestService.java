package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.dto.request.RequestDto;
import ru.practicum.explorewithme.dto.request.RequestStatus;

import java.util.List;

public interface RequestService {
    List<RequestDto> getRequestsToUsersEvent(long eventId);

    List<RequestDto> changeRequestStatuses(List<Long> requestIds, RequestStatus status);

    List<RequestDto> getRequestsByIds(List<Long> requestIds);

    ParticipationRequestDto create(String userId, String eventId);

    ParticipationRequestDto cancelRequest(String userId, String requestId);

    List<ParticipationRequestDto> getUserRequests(String requesterId);
}
