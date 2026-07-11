package ru.practicum.explorewithme.controller.priv;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.exception.IdNotFoundException;
import ru.practicum.explorewithme.service.RequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/requests")
@RequiredArgsConstructor
public class PrivateRequestController {
    private final RequestService service;

    private static final Logger log = LoggerFactory.getLogger(PrivateRequestController.class);

    @PostMapping
    public ResponseEntity<ParticipationRequestDto> addParticipationRequest(@PathVariable String userId, @RequestParam(name = "eventId") String eventId) {
        if (eventId == null || eventId.isBlank() || eventId.equals("0")) {
            throw new IdNotFoundException(0);
        }

        ParticipationRequestDto createdRequest = service.create(userId, eventId);
        log.info("Создан запрос на участие с именем {}.", createdRequest.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createdRequest);
    }

    @GetMapping
    public List<ParticipationRequestDto> getUserRequests(@PathVariable String userId) {
        return service.getUserRequests(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelRequest(@PathVariable String userId, @PathVariable String requestId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(service.cancelRequest(userId, requestId));
    }

}
