package ru.practicum.explorewithme.controller.priv;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.service.RequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/requests")
@RequiredArgsConstructor
public class PrivateRequestController {
    private final RequestService service;

    private static final Logger log = LoggerFactory.getLogger(PrivateRequestController.class);

    @PostMapping
    public ParticipationRequestDto addParticipationRequest(@PathVariable String userId, @RequestParam(name = "eventId") String eventId) {
        ParticipationRequestDto createdRequest = service.create(userId, eventId);
        log.info("Создано бронирование с именем {}.", createdRequest.getId());
        return createdRequest;
    }

    @GetMapping
    public List<ParticipationRequestDto> getUserRequests(@PathVariable String userId) {
        return service.getUserRequests(userId);
    }
//
//    @PatchMapping("/{requestId}/cancel")
//    public ParticipationRequestDto cancelRequest() {
//        ParticipationRequestDto request = service.cancelRequest();
//        log.info("Обновлено бронирование с идентификатором {}.", bookingId);
//        return request;
//    }

}
