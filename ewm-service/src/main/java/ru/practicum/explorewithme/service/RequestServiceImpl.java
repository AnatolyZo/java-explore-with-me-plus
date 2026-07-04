package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.RequestDto;
import ru.practicum.explorewithme.dto.RequestStatus;
import ru.practicum.explorewithme.entity.Request;
import ru.practicum.explorewithme.exception.UnavailableUpdateException;
import ru.practicum.explorewithme.mapper.RequestMapper;
import ru.practicum.explorewithme.repository.RequestRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;

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
        List<Request> requests = requestRepository.findByRequestIdIn(requestIds);

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
        List<Request> requests = requestRepository.findByRequestIdIn(requestIds);
        return requests.stream()
                .map(RequestMapper::mapToRequestDto)
                .toList();
    }
}
