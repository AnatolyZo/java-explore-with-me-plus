package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.EventStatus;
import ru.practicum.explorewithme.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.dto.RequestDto;
import ru.practicum.explorewithme.dto.RequestStatus;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.entity.Request;
import ru.practicum.explorewithme.entity.User;
import ru.practicum.explorewithme.exception.DuplicatedDataException;
import ru.practicum.explorewithme.exception.IdNotFoundException;
import ru.practicum.explorewithme.exception.UnavailableUpdateException;
import ru.practicum.explorewithme.mapper.ParticipationRequestMapper;
import ru.practicum.explorewithme.mapper.RequestMapper;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.RequestRepository;
import ru.practicum.explorewithme.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {
    private final ParticipationRequestMapper requestMapper;
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public List<RequestDto> getRequestsToUsersEvent(long eventId) {
        List<Request> requests = requestRepository.findByEventId(eventId);
        return requests.stream()
                .map(RequestMapper::mapToRequestDto)
                .toList();
    }

    @Override
    @Transactional
    public List<RequestDto> changeRequestStatuses(List<Long> requestIds, RequestStatus status) {
        List<Request> requests = requestRepository.findByRequesterIdIn(requestIds);

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
                .map(RequestMapper::mapToRequestDto)
                .toList();
    }

    @Override
    public List<RequestDto> getRequestsByIds(List<Long> requestIds) {
        List<Request> requests = requestRepository.findByRequesterIdIn(requestIds);
        return requests.stream()
                .map(RequestMapper::mapToRequestDto)
                .toList();
    }


    @Override
    @Transactional
    public ParticipationRequestDto create(String userId, String eventId) {
        Request request = new Request();

        LocalDateTime now = LocalDateTime.now();

        Long userIdL = Long.parseLong(userId);
        Long eventIdL = Long.parseLong(eventId);

        Optional<Request> optRequest = requestRepository.findByEventIdAndRequesterId(eventIdL, userIdL);

        if (optRequest.isPresent()) {
            throw new DuplicatedDataException("request", "eventId and userId", eventId);
        }

        Event event = eventRepository.findById(eventIdL)
                .orElseThrow(() -> new IdNotFoundException(eventIdL));

        if (event.getInitiator() != null
                && Objects.equals(event.getInitiator().getId(), userIdL)) {
            throw new DuplicatedDataException("request", "eventId and userId", eventId);
        }

        int count = Math.toIntExact(requestRepository.countByEventId(eventIdL));

        if (event.getParticipantLimit() != 0 && count >= event.getParticipantLimit()) {
            throw new DuplicatedDataException("request", "eventId and userId", eventId);
        }

        if (!event.getStatus().equals(EventStatus.PUBLISHED)) {
            throw new DuplicatedDataException("request", "eventId and userId", eventId);
        }

        User requestor = userRepository.findById(userIdL)
                .orElseThrow(() -> new IdNotFoundException(userIdL));

        request.setCreated(now);
        request.setRequester(requestor);
        request.setEvent(event);

        if (event.getParticipantLimit() == 0 || !event.isRequestModeration()) {
            request.setStatus(RequestStatus.CONFIRMED);
        } else {
            request.setStatus(RequestStatus.PENDING);
        }
        Request createdRequest = requestRepository.save(request);
        return requestMapper.toParticipationRequestDto(createdRequest);
    }


    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(String userId, String requestId) {
        Long userIdL = Long.parseLong(userId);
        Long requestIdL = Long.parseLong(requestId);


        Request request = requestRepository.findById(requestIdL)
                .orElseThrow(() -> new IdNotFoundException(requestIdL));

        if (request.getStatus().equals(RequestStatus.CONFIRMED)) {
            throw new DuplicatedDataException("request", "status", RequestStatus.CONFIRMED);
        }

        request.setStatus(RequestStatus.CANCELED);
        Request updatedRequest = requestRepository.save(request);
        return requestMapper.toParticipationRequestDto(updatedRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getUserRequests(String requesterId) {
        Long requesterIdL = Long.parseLong(requesterId);

        List<Request> requests = requestRepository.findAllByRequesterIdOrderByCreatedDesc(requesterIdL);

        return requests.stream()
                .map(requestMapper::toParticipationRequestDto)
                .toList();
    }
}
