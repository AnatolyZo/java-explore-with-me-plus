package ru.practicum.explorewithme.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.ExploreWithMeMainService;
import ru.practicum.explorewithme.dto.EventDto;
import ru.practicum.explorewithme.dto.UpdateEventDto;
import ru.practicum.explorewithme.service.EventService;

import java.util.List;

@RestController
@RequestMapping(path = AdminEventController.URL_BASE)
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class AdminEventController {
    public static final String URL_BASE = ExploreWithMeMainService.URL_ADMIN + "/events";
    public static final String PATH_VAR_ID = "eventId";
    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<EventDto>> searchEvents(@RequestParam(required = false) List<Long> users,
                                                       @RequestParam(required = false) List<String> states,
                                                       @RequestParam(required = false) List<Long> categories,
                                                       @RequestParam(required = false) String rangeStart,
                                                       @RequestParam(required = false) String rangeEnd,
                                                       @RequestParam(defaultValue = "0") int from,
                                                       @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(eventService.searchEvents(users, states, categories, rangeStart, rangeEnd, from, size));
    }

    @PatchMapping("/{" + PATH_VAR_ID + "}")
    public ResponseEntity<EventDto> updateEvent(@PathVariable long eventId,
                                                @RequestBody UpdateEventDto updateEventDto) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(eventService.updateEventByAdmin(eventId, updateEventDto));
    }
}
