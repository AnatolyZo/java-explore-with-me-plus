package ru.practicum.explorewithme.controller.priv;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.request.RequestDto;
import ru.practicum.explorewithme.service.RequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/users/{userId}/requests")
@RequiredArgsConstructor
@Validated
public class PrivateRequestController {
    private final RequestService service;

    private static final Logger log = LoggerFactory.getLogger(PrivateRequestController.class);

    @PostMapping
    public ResponseEntity<RequestDto> addRequest(@PathVariable @NotNull @Positive Long userId,
                                                 @RequestParam(name = "eventId") @NotNull @Positive Long eventId) {
        RequestDto createdRequest = service.create(userId, eventId);
        log.info("Создан запрос на участие с именем {}.", createdRequest.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createdRequest);
    }

    @GetMapping
    public List<RequestDto> getUserRequests(@PathVariable @NotNull @Positive Long userId) {
        return service.getUserRequests(userId);
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<RequestDto> cancelRequest(@PathVariable @NotNull @Positive Long userId,
                                                    @PathVariable @NotNull @Positive Long requestId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(service.cancelRequest(userId, requestId));
    }

}
