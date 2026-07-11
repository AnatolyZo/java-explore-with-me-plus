package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.event.EventStatus;
import ru.practicum.explorewithme.dto.request.RequestDto;
import ru.practicum.explorewithme.dto.request.RequestStatus;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.entity.Request;
import ru.practicum.explorewithme.entity.User;
import ru.practicum.explorewithme.exception.DuplicatedDataException;
import ru.practicum.explorewithme.exception.UnavailableUpdateException;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.RequestRepository;
import ru.practicum.explorewithme.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl extends ServiceBase implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public List<RequestDto> getRequestsToUsersEvent(long eventId) {
        List<Request> requests = requestRepository.findByEventId(eventId);
        return requests.stream()
                .map(request -> RequestDto.builder()
                        .id(request.getId())
                        .created(request.getCreated())
                        .event(request.getEvent().getId())
                        .requester(request.getRequester().getId())
                        .status(request.getStatus())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public List<RequestDto> changeRequestStatuses(List<Long> requestIds, RequestStatus status) {
        List<Request> requests = requestRepository.findByIdIn(requestIds);
        Optional<Long> nonPendingRequestId = requests.stream()
                .filter(request -> !request.getStatus().equals(RequestStatus.PENDING))
                .findFirst()
                .map(Request::getId);
        //Реализация требования "статус можно изменить только у заявок, находящихся в состоянии ожидания"
        //не совсем понятно как быть, если не все заявки в режиме ожидания, временная реализация до тестов
        if (nonPendingRequestId.isPresent()) {
            throw new UnavailableUpdateException("Request", nonPendingRequestId.get());
        }

        requests.forEach(request -> request.setStatus(status));

        return requests.stream()
                .map(request -> RequestDto.builder()
                        .id(request.getId())
                        .created(request.getCreated())
                        .event(request.getEvent().getId())
                        .requester(request.getRequester().getId())
                        .status(request.getStatus())
                        .build())
                .toList();
    }

    @Override
    public List<RequestDto> getRequestsByIds(List<Long> requestIds) {
        List<Request> requests = requestRepository.findByIdIn(requestIds);
        return requests.stream()
                .map(request -> RequestDto.builder()
                        .id(request.getId())
                        .created(request.getCreated())
                        .event(request.getEvent().getId())
                        .requester(request.getRequester().getId())
                        .status(request.getStatus())
                        .build())
                .toList();
    }


    @Override
    @Transactional
    public RequestDto create(long userId, long eventId) {
        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new DuplicatedDataException("request", "eventId and userId", eventId);
        }

        Event event = findEntityIn(eventRepository, eventId);
        requestChecks(event, userId);
        User requester = findEntityIn(userRepository, userId);
        RequestStatus status = changeRequestStatus(event);

        Request request = Request.builder()
                .created(LocalDateTime.now())
                .event(event)
                .requester(requester)
                .status(status)
                .build();

        Request createdRequest = requestRepository.save(request);
        incrementEventConfirmedRequests(createdRequest, event);

        return RequestDto.builder()
                .id(createdRequest.getId())
                .created(createdRequest.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(createdRequest.getStatus())
                .build();
    }


    @Override
    @Transactional
    public RequestDto cancelRequest(long userId, long requestId) {
        Request request = findEntityIn(requestRepository, requestId);

        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
            throw new DuplicatedDataException("request", "status", RequestStatus.CONFIRMED);
        }

        request.setStatus(RequestStatus.CANCELED);
        Request updatedRequest = requestRepository.save(request);
        decrementEventConfirmedRequests(updatedRequest);
        return RequestDto.builder()
                .id(updatedRequest.getId())
                .created(updatedRequest.getCreated())
                .event(request.getEvent().getId())
                .requester(request.getRequester().getId())
                .status(updatedRequest.getStatus())
                .build();
    }

    @Override
    public List<RequestDto> getUserRequests(long requesterId) {
        List<Request> requests = requestRepository.findByRequesterIdOrderByCreatedDesc(requesterId);

        return requests.stream()
                .map(request -> RequestDto.builder()
                        .id(request.getId())
                        .created(request.getCreated())
                        .event(request.getEvent().getId())
                        .requester(request.getRequester().getId())
                        .status(request.getStatus())
                        .build())
                .toList();
    }

    private void requestChecks(Event event, long userId) {
        if (event.getInitiator().getId() == userId) {
            throw new DuplicatedDataException("request", "eventId and userId", event.getId());
        }

        if (!event.getStatus().equals(EventStatus.PUBLISHED)) {
            throw new DuplicatedDataException("request", "eventId and userId", event.getId());
        }

        if (event.getParticipantLimit() - event.getConfirmedRequests() == 0 && event.getParticipantLimit() != 0) {
            throw new DuplicatedDataException("request", "eventId and userId", event.getId());
        }
    }

    private RequestStatus changeRequestStatus(Event event) {
        RequestStatus status;

        if (event.getParticipantLimit() == 0 || !event.isRequestModeration()) {
            status = RequestStatus.CONFIRMED;
        } else {
            status = RequestStatus.PENDING;
        }

        return status;
    }

    private void incrementEventConfirmedRequests(Request request, Event event) {
        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
            int confirmedRequests = event.getConfirmedRequests();
            event.setConfirmedRequests(++confirmedRequests);
            Event e = eventRepository.save(event);
        }
    }

    private void decrementEventConfirmedRequests(Request request) {
        if (request.getStatus().equals(RequestStatus.CANCELED)) {
            Event event = findEntityIn(eventRepository, request.getEvent().getId());
            int confirmedRequests = event.getConfirmedRequests();
            event.setConfirmedRequests(--confirmedRequests);
            eventRepository.save(event);
        }
    }
}
