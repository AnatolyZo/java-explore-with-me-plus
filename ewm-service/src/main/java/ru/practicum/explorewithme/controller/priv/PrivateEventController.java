package ru.practicum.explorewithme.controller.priv;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.ExploreWithMeMainService;
import ru.practicum.explorewithme.dto.*;
import ru.practicum.explorewithme.service.EventService;

import java.util.List;

@RestController
@RequestMapping(path = PrivateEventController.URL_BASE)
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class PrivateEventController {
    public static final String URL_BASE = ExploreWithMeMainService.URL_PRIVATE + "/events";
    public static final String EVENT_ID = "eventId";
    private static final String API_PREFIX_REQUESTS = "/requests";
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventDto>> getEvents(@PathVariable long userId,
                                                    @RequestParam(defaultValue = "0") int from,
                                                    @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(eventService.getEvents(userId, from, size));
    }

    @PostMapping
    public ResponseEntity<EventDto> createEvent(@PathVariable long userId,
                                                @Valid @RequestBody NewEventDto newEventDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(userId, newEventDto));
    }

    @GetMapping("/{" + EVENT_ID + "}")
    public ResponseEntity<EventDto> getEvent(@PathVariable long userId,
                                             @PathVariable long eventId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(eventService.getEvent(userId, eventId));
    }

    @PatchMapping("/{" + EVENT_ID + "}")
    public ResponseEntity<EventDto> updateEvent(@PathVariable long userId,
                                                @PathVariable long eventId,
                                                @Valid @RequestBody UpdateEventDto updateEventDto) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(eventService.updateEvent(userId, eventId, updateEventDto));
    }

    @GetMapping("/{" + EVENT_ID + "}" + API_PREFIX_REQUESTS)
    public ResponseEntity<List<RequestDto>> getRequests(@PathVariable long userId,
                                                        @PathVariable long eventId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(eventService.getRequests(userId, eventId));
    }

    @PatchMapping("/{" + EVENT_ID + "}" + API_PREFIX_REQUESTS)
    public ResponseEntity<ChangedRequestStatusesDto> updateRequestStatuses(@PathVariable long userId,
                                                                  @PathVariable long eventId,
                                                                  @RequestBody UpdateRequestStatusDto update) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(eventService.updateRequestStatuses(userId, eventId, update));
    }
}
