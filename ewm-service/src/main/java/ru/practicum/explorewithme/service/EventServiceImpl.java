package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.StatsClient;
import ru.practicum.explorewithme.common.pagination.OffsetPageRequest;
import ru.practicum.explorewithme.dto.*;
import ru.practicum.explorewithme.entity.Category;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.entity.LocationEmbeddable;
import ru.practicum.explorewithme.entity.User;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.exception.UnavailableUpdateException;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.mapper.LocationMapper;
import ru.practicum.explorewithme.repository.EventRepository;
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
    private final StatsClient statsClient;
    private static final String API_PREFIX_EVENTS = "/events/";

    @Override
    public List<EventDto> getEvents(long userId, int from, int size) {
        Pageable pageable = new OffsetPageRequest(from, size);

        List<Event> events = eventRepository.findByInitiatorId(userId, pageable);

        //Получаем статистику
        List<ViewStatsResponse> stats = getStats(events, userId);

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

    @Override
    @Transactional
    public EventDto createEvent(long userId, NewEventDto newEventDto) {
        //Получаем данные по категории, инициатору и месту проведения события
        Category category = categoryService.findCategoryBy(newEventDto.getCategory());
        User initiator = userService.getUserById(userId);
        Location location = newEventDto.getLocation();
        LocationEmbeddable locationEmbeddable = LocationMapper.mapToLocationEmbeddable(location.lat(), location.lon());

        //Формируем событие и сохраняем
        Event event = EventMapper.mapToEvent(newEventDto, category, initiator, locationEmbeddable);
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
        Event event = getEventById(eventId, userId);

        //Проверяем, что событие отменено или ожидает модерации
        if (!event.getStatus().equals(EventStatus.CANCELLED) && !event.getStatus().equals(EventStatus.PENDING)) {
            throw new UnavailableUpdateException(eventId);
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
        return List.of();
    }

    @Override
    @Transactional
    public List<RequestDto> updateRequestStatuses(long userId, long eventId, UpdateRequestStatusDto update) {
        return List.of();
    }

    @Override
    public Event getEventById(long eventId, long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event", eventId));
    }

    private List<ViewStatsResponse> getStats(List<Event> events, long userId) {
        //Определяем самую раннюю дату создания события пользователем
        LocalDateTime earliestDate = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElseThrow(() -> new NotFoundException("event from user", userId));

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

        event.setAnnotation(status.name());

        if (update.getTitle() != null && !update.getTitle().isEmpty()) {
            event.setTitle(update.getTitle());
        }
    }
}
