package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.dto.RequestDto;
import ru.practicum.explorewithme.dto.RequestStatus;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.entity.Request;
import ru.practicum.explorewithme.entity.User;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.exception.UnavailableUpdateException;
import ru.practicum.explorewithme.mapper.ParticipationRequestMapper;
import ru.practicum.explorewithme.mapper.RequestMapper;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.RequestRepository;
import ru.practicum.explorewithme.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
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

        Event event = eventRepository.findById(eventIdL)
                .orElseThrow(() -> new NotFoundException("Event", eventIdL));

        User requestor = userRepository.findById(userIdL)
                .orElseThrow(() -> new NotFoundException("User", userIdL));

        request.setCreated(now);
        request.setRequester(requestor);
        request.setEvent(event);
        Request createdRequest = requestRepository.save(request);
        return requestMapper.toParticipationRequestDto(createdRequest);
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
