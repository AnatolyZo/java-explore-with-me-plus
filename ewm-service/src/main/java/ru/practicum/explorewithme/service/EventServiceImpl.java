package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.explorewithme.StatsClient;
import ru.practicum.explorewithme.common.pagination.OffsetPageRequest;
import ru.practicum.explorewithme.dto.*;
import ru.practicum.explorewithme.entity.Category;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.entity.LocationEmbeddable;
import ru.practicum.explorewithme.entity.User;
import ru.practicum.explorewithme.exception.BadRequestException;
import ru.practicum.explorewithme.exception.EarlyDateException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.exception.UnavailableUpdateException;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.mapper.LocationMapper;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.EventSearchSpecification;
import ru.practicum.explorewithme.repository.specification.EventSpecifications;
import ru.practicum.explorewithme.hit.EndpointHitRequest;
import ru.practicum.explorewithme.stats.ViewStatsResponse;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final CategoryService categoryService;
    private final UserService userService;
    private final RequestService requestService;
    private final StatsClient statsClient;
    private static final String API_PREFIX_EVENTS = "/events/";
    private static final String APP_NAME = "ewm-main-service";

    @Override
    public List<EventDto> getEvents(long userId, int from, int size) {
        Pageable pageable = new OffsetPageRequest(from, size);

        List<Event> events = eventRepository.findByInitiatorId(userId, pageable);

        Map<String, Long> viewsByUri = getStats(events);

        return getEventsWithStats(events, viewsByUri);
    }

    @Override
    @Transactional
    public EventDto createEvent(long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate() != null) {
            int usersMinOffset = 2;
            checkTimeBeforeEventStart(newEventDto.getEventDate(), usersMinOffset);
        }
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!" + newEventDto);
        //Получаем данные по категории, инициатору и месту проведения события
        Category category = categoryService.findCategoryBy(newEventDto.getCategory());
        User initiator = userService.getUserById(userId);
        Location location = newEventDto.getLocation();
        LocationEmbeddable locationEmbeddable = LocationMapper.mapToLocationEmbeddable(location.lat(), location.lon());

        //Формируем событие и сохраняем
        Event event = EventMapper.mapToEvent(newEventDto, category, initiator, locationEmbeddable);
        event.setCreatedOn(LocalDateTime.now());
        event.setStatus(EventStatus.PENDING);
        event.setRequestModeration(true);

        Event createdEvent = eventRepository.save(event);
        return EventMapper.mapToEventDto(createdEvent,0);
    }

    @Override
    public EventDto getEvent(long userId, long eventId) {
        Event event = getEventById(eventId, userId);

        Map<String, Long> viewsByUri = getStats(List.of(event));

        return EventMapper.mapToEventDto(event, getViews(event, viewsByUri));
    }

    @Override
    @Transactional
    public EventDto updateEvent(long userId, long eventId, UpdateEventDto body) {
        if (body.getEventDate() != null) {
            int usersMinOffset = 2;
            checkTimeBeforeEventStart(body.getEventDate(), usersMinOffset);
        }

        Event event = getEventById(eventId, userId);

        //Проверяем, что событие отменено или ожидает модерации
        if (!event.getStatus().equals(EventStatus.CANCELED) && !event.getStatus().equals(EventStatus.PENDING)) {
            throw new UnavailableUpdateException("Event", eventId);
        }

        EventStatus status;

        //Проверяем хочет ли пользователь отменить событие
        if (body.getStatus() != null && body.getStatus().equals(EventUpdateAction.CANCEL_REVIEW)) {
            status = EventStatus.CANCELED;
        } else {
            status = EventStatus.PENDING;
        }

        updateEventFields(event, body, status);

        Event updatedEvent = eventRepository.save(event);

        Map<String, Long> viewsByUri = getStats(List.of(event));

        return EventMapper.mapToEventDto(updatedEvent, getViews(event, viewsByUri));
    }

    @Override
    public List<RequestDto> getRequests(long userId, long eventId) {
        checkEventExistence(userId, eventId);

        return requestService.getRequestsToUsersEvent(eventId);
    }

    @Override
    @Transactional
    public ChangedRequestStatusesDto updateRequestStatuses(long userId, long eventId, UpdateRequestStatusDto update) {
        Event event = getEventById(userId, eventId);

        //Обработка ситуации, когда не установлено ограничение по количеству участников
        //или запрос не требует модерации
        if (event.getParticipantLimit() == 0 || !event.isRequestModeration()) {
            List<Long> requestIds = update.getRequestIds();
            List<RequestDto> confirmedRequests = requestService.getRequestsByIds(requestIds);

            return ChangedRequestStatusesDto.builder()
                    .confirmedRequests(confirmedRequests)
                    .rejectedRequests(List.of())
                    .build();
        }

        //Вычисление свободных мест для посещения события
        int requestsAvailableToConfirm = event.getParticipantLimit() - event.getConfirmedRequests();

        if (requestsAvailableToConfirm == 0) {
            throw new UnavailableUpdateException("Event", eventId);
        }

        //Обработка случая подтверждения запросов
        if (update.getStatus().equals(RequestStatus.CONFIRMED)) {
            //Разделяем запросы на те, которые можем одобрить и отклонить, исходя из количества доступных мест
            List<Long> requestsToConfirm = update.getRequestIds().stream()
                    .limit(requestsAvailableToConfirm)
                    .toList();

            List<Long> requestsToReject = update.getRequestIds().stream()
                    .skip(requestsAvailableToConfirm)
                    .toList();

            List<RequestDto> confirmedRequests = requestService.changeRequestStatuses(requestsToConfirm, RequestStatus.CONFIRMED);
            List<RequestDto> rejectedRequests = requestService.changeRequestStatuses(requestsToReject, RequestStatus.REJECTED);

            //Обновляем количество свободных мест
            event.setConfirmedRequests(event.getConfirmedRequests() + confirmedRequests.size());
            eventRepository.save(event);

            return ChangedRequestStatusesDto.builder()
                    .confirmedRequests(confirmedRequests)
                    .rejectedRequests(rejectedRequests)
                    .build();
        }

        //Обработка случая отклонения запросов
        List<RequestDto> rejectedRequests = requestService.changeRequestStatuses(update.getRequestIds(), RequestStatus.REJECTED);

        return ChangedRequestStatusesDto.builder()
                .confirmedRequests(List.of())
                .rejectedRequests(rejectedRequests)
                .build();
    }

    @Override
    public List<EventShortDto> getPublishedEvents(String text,
                                                   List<Long> categories,
                                                   Boolean paid,
                                                   LocalDateTime rangeStart,
                                                   LocalDateTime rangeEnd,
                                                   boolean onlyAvailable,
                                                   PublicEventSort sort,
                                                   int from,
                                                   int size,
                                                   String ip,
                                                   String uri) {
        checkDateRange(rangeStart, rangeEnd);
        saveHit(ip, uri);

        LocalDateTime start = rangeStart;
        if (rangeStart == null && rangeEnd == null) {
            start = LocalDateTime.now();
        }

        Specification<Event> specification = Specification.<Event>unrestricted()
                .and(EventSpecifications.hasStatus(EventStatus.PUBLISHED))
                .and(EventSpecifications.textContains(text))
                .and(EventSpecifications.categoryIn(categories))
                .and(EventSpecifications.paidEquals(paid))
                .and(EventSpecifications.eventDateFrom(start))
                .and(EventSpecifications.eventDateTo(rangeEnd))
                .and(EventSpecifications.onlyAvailable(onlyAvailable));

        Sort eventsSort = sort == PublicEventSort.EVENT_DATE ? Sort.by("eventDate").ascending() : Sort.unsorted();
        List<Event> events = eventRepository.findAll(specification, eventsSort);

        if (events.isEmpty()) {
            return List.of();
        }

        Map<String, Long> viewsByUri = getStats(events);

        List<EventShortDto> dtos = events.stream()
                .map(event -> EventMapper.mapToEventShortDto(event, getViews(event, viewsByUri)))
                .toList();

        if (sort == PublicEventSort.VIEWS) {
            dtos = dtos.stream()
                    .sorted(Comparator.comparingLong(EventShortDto::getViews).reversed())
                    .toList();
        }

        return getPage(dtos, from, size);
    }

    @Override
    public EventDto getPublishedEvent(long eventId, String ip, String uri) {
        saveHit(ip, uri);
        Event event = eventRepository.findByIdAndStatus(eventId, EventStatus.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event", eventId));
        Map<String, Long> viewsByUri = getStats(List.of(event));

        return EventMapper.mapToEventDto(event, getViews(event, viewsByUri));
    }

    @Override
    public Event getEventById(long eventId, long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event", eventId));
    }

    @Override
    public List<EventDto> searchEvents(List<Long> users,
                                       List<String> states,
                                       List<Long> categories,
                                       String rangeStart,
                                       String rangeEnd,
                                       int from,
                                       int size) {
        Specification<Event> spec = new EventSearchSpecification(users, states, categories, rangeStart, rangeEnd);
        Pageable pageable = new OffsetPageRequest(from, size);
        Page<Event> eventPage = eventRepository.findAll(spec, pageable);
        List<Event> events = eventPage.getContent();

        if (events.isEmpty()) {
            return List.of();
        }

        //Получаем статистику
        Map<String, Long> viewsByUri = getStats(events);
        return getEventsWithStats(events, viewsByUri);
    }

    @Override
    @Transactional
    public EventDto updateEventByAdmin(long eventId, UpdateEventDto body) {
        if (body.getEventDate() != null) {
            int adminsMinOffset = 1;
            checkTimeBeforeEventStart(body.getEventDate(), adminsMinOffset);
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event", eventId));


        if (!event.getStatus().equals(EventStatus.PENDING)) {
            throw new UnavailableUpdateException("Event", eventId);
        }

        EventStatus status;

        if (body.getStatus() != null && body.getStatus().equals(EventUpdateAction.PUBLISH_EVENT)) {
            status = EventStatus.PUBLISHED;
        } else if (body.getStatus() != null && body.getStatus().equals(EventUpdateAction.REJECT_EVENT)) {
            status = EventStatus.CANCELED;
        } else {
            status = event.getStatus();
        }

        updateEventFields(event, body, status);

        Event updatedEvent = eventRepository.save(event);

        Map<String, Long> viewsByUri = getStats(List.of(updatedEvent));

        return EventMapper.mapToEventDto(updatedEvent, getViews(updatedEvent, viewsByUri));
    }

    private Map<String, Long> getStats(List<Event> events) {
        //Определяем самую раннюю дату создания события пользователем
        LocalDateTime start = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No events were found"));

        //Верхняя граница для поиска
        LocalDateTime end = LocalDateTime.now();

        //Получаем список uri для отправки в сервер статистики
        List<String> uris = events.stream()
                .map(event -> API_PREFIX_EVENTS + event.getId())
                .toList();

        List<ViewStatsResponse> stats = statsClient.getStatistics(start, end, uris, false);
        Map<String, Long> viewsByUri = new HashMap<>();
        stats.forEach(stat -> viewsByUri.put(stat.getUri(), stat.getHits()));

        return viewsByUri;
    }

    private long getViews(Event event, Map<String, Long> viewsByUri) {
        return viewsByUri.getOrDefault(API_PREFIX_EVENTS + event.getId(), 0L);
    }

    private <T> List<T> getPage(List<T> source, int from, int size) {
        if (from >= source.size()) {
            return List.of();
        }

        int toIndex = Math.min(from + size, source.size());
        return source.subList(from, toIndex);
    }

    private void saveHit(String ip, String uri) {
        statsClient.addStatistics(EndpointHitRequest.builder()
                .app(APP_NAME)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build());
    }

    private void checkDateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("rangeStart must be before rangeEnd");
        }
    }

    private List<EventDto> getEventsWithStats(List<Event> events, Map<String, Long> viewsByUri) {
        return events.stream()
                .map(event -> EventMapper.mapToEventDto(event, getViews(event, viewsByUri)))
                .toList();
    }

    private void updateEventFields(Event event, UpdateEventDto update, EventStatus status) {
        if (update.getAnnotation() != null && !update.getAnnotation().isEmpty()) {
            event.setAnnotation(update.getAnnotation());
        }

        if (update.getDescription() != null && !update.getDescription().isEmpty()) {
            event.setDescription(update.getDescription());
        }

        if (update.getEventDate() != null) {
            event.setEventDate(update.getEventDate());
        }

        if (update.getLocation() != null) {
            LocationEmbeddable location = LocationMapper.mapToLocationEmbeddable(update.getLocation().lat(), update.getLocation().lon());
            event.setLocation(location);
        }

        if (update.getPaid() != null) {
            event.setPaid(update.getPaid());
        }

        if (update.getAnnotation() != null && !update.getAnnotation().isEmpty()) {
            event.setAnnotation(update.getAnnotation());
        }

        if (update.getParticipantLimit() != null) {
            event.setParticipantLimit(update.getParticipantLimit());
        }

        if (update.getRequestModeration() != null) {
            event.setRequestModeration(update.getRequestModeration());
        }

        event.setStatus(status);

        if (update.getTitle() != null && !update.getTitle().isEmpty()) {
            event.setTitle(update.getTitle());
        }
    }

    private void checkEventExistence(long userId, long eventId) {
        if (!eventRepository.existsByIdAndInitiatorId(eventId, userId)) {
            throw new NotFoundException("event from user", userId);
        }
    }

    private void checkTimeBeforeEventStart(LocalDateTime eventDate, int minOffset) {
        if (!eventDate.isAfter(LocalDateTime.now().plusHours(minOffset))) {
            throw new EarlyDateException(eventDate);
        }
    }
}
