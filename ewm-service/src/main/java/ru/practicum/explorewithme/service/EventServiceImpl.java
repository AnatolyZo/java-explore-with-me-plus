package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import ru.practicum.explorewithme.exception.EarlyDateException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.exception.UnavailableUpdateException;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.mapper.LocationMapper;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.EventSearchSpecification;
import ru.practicum.explorewithme.stats.ViewStatsResponse;

import java.time.LocalDateTime;
import java.util.List;

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

    @Override
    public List<EventDto> getEvents(long userId, int from, int size) {
        Pageable pageable = new OffsetPageRequest(from, size);

        List<Event> events = eventRepository.findByInitiatorId(userId, pageable);

        //Получаем статистику
        List<ViewStatsResponse> stats = getStats(events, userId);

        return getEventsWithStats(events, stats);
    }

    @Override
    @Transactional
    public EventDto createEvent(long userId, NewEventDto newEventDto) {
        if (newEventDto.getEventDate() != null) {
            int usersMinOffset = 2;
            checkTimeBeforeEventStart(newEventDto.getEventDate(), usersMinOffset);
        }

        //Получаем данные по категории, инициатору и месту проведения события
        Category category = categoryService.findCategoryBy(newEventDto.getCategory());
        User initiator = userService.getUserById(userId);
        Location location = newEventDto.getLocation();
        LocationEmbeddable locationEmbeddable = LocationMapper.mapToLocationEmbeddable(location.lat(), location.lon());

        //Формируем событие и сохраняем
        Event event = EventMapper.mapToEvent(newEventDto, category, initiator, locationEmbeddable);
        event.setCreatedOn(LocalDateTime.now());
        event.setStatus(EventStatus.PENDING);

        Event createdEvent = eventRepository.save(event);
        return EventMapper.mapToEventDto(createdEvent,0);
    }

    @Override
    public EventDto getEvent(long userId, long eventId) {
        Event event = getEventById(userId, eventId);

        ViewStatsResponse stats = getStats(event);
        long views = stats.getHits();

        return EventMapper.mapToEventDto(event, views);
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
        if (!event.getStatus().equals(EventStatus.CANCELLED) && !event.getStatus().equals(EventStatus.PENDING)) {
            throw new UnavailableUpdateException("Event", eventId);
        }

        //Проверяем хочет ли пользователь отменить событие
        if (body.getStatus().equals(EventUpdateAction.CANCEL)) {
            updateEventFields(event, body, EventStatus.CANCELLED);
        } else {
            updateEventFields(event, body, EventStatus.PENDING);
        }

        Event updatedEvent = eventRepository.save(event);

        ViewStatsResponse stats = getStats(event);
        long views = stats.getHits();

        return EventMapper.mapToEventDto(updatedEvent, views);
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
        List<ViewStatsResponse> stats = getStats(events, null);
        return getEventsWithStats(events, stats);
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

        if (body.getStatus().equals(EventUpdateAction.PUBLISH)) {
            status = EventStatus.PUBLISHED;
        } else if (body.getStatus().equals(EventUpdateAction.REJECT)) {
            status = EventStatus.CANCELLED;
        } else {
            throw new UnavailableUpdateException("Event", eventId);
        }

        updateEventFields(event, body, status);

        Event updatedEvent = eventRepository.save(event);

        ViewStatsResponse stats = getStats(updatedEvent);
        long views = stats.getHits();

        return EventMapper.mapToEventDto(updatedEvent, views);
    }

    private List<ViewStatsResponse> getStats(List<Event> events, Long userId) {
        //Определяем самую раннюю дату создания события пользователем
        LocalDateTime earliestDate = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No events were found"));

        //Верхняя граница для поиска
        LocalDateTime now = LocalDateTime.now();

        //Получаем список uri для отправки в сервер статистики
        List<String> uris = events.stream()
                .map(event -> API_PREFIX_EVENTS + event.getId())
                .toList();

        return statsClient.getStatistics(earliestDate, now, uris, false);
    }

    private ViewStatsResponse getStats(Event event) {
        LocalDateTime earliestDate = event.getCreatedOn();
        LocalDateTime now = LocalDateTime.now();
        List<String> uri = List.of(API_PREFIX_EVENTS + event.getId());

        List<ViewStatsResponse> stats = statsClient.getStatistics(earliestDate, now, uri, false);
        return stats.getFirst();
    }

    private List<EventDto> getEventsWithStats(List<Event> events, List<ViewStatsResponse> stats) {
        return events.stream()
                .map(event -> {
                    //Находим нужную статистику для события
                    String targetUri = API_PREFIX_EVENTS + event.getId();

                    long views = stats.stream()
                            .filter(stat -> targetUri.equals(stat.getUri()))
                            .findFirst()
                            .map(ViewStatsResponse::getHits)
                            .orElse(0L);

                    return EventMapper.mapToEventDto(event, views);
                })
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
