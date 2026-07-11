package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.ExploreWithMeMainService;
import ru.practicum.explorewithme.StatsClient;
import ru.practicum.explorewithme.common.pagination.OffsetPageRequest;
import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.event.*;
import ru.practicum.explorewithme.dto.request.ChangedRequestStatusesDto;
import ru.practicum.explorewithme.dto.request.RequestDto;
import ru.practicum.explorewithme.dto.request.RequestStatus;
import ru.practicum.explorewithme.dto.request.UpdateRequestStatusDto;
import ru.practicum.explorewithme.dto.user.UserShortDto;
import ru.practicum.explorewithme.entity.Category;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.entity.LocationEmbeddable;
import ru.practicum.explorewithme.entity.User;
import ru.practicum.explorewithme.exception.WrongDateIntervalException;
import ru.practicum.explorewithme.exception.EarlyDateException;
import ru.practicum.explorewithme.exception.IdNotFoundException;
import ru.practicum.explorewithme.exception.UnavailableUpdateException;
import ru.practicum.explorewithme.hit.EndpointHitRequest;
import ru.practicum.explorewithme.mapper.CategoryMapper;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.mapper.LocationMapper;
import ru.practicum.explorewithme.mapper.UserMapper;
import ru.practicum.explorewithme.repository.CategoryRepository;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.specification.AdminEventSearchSpecification;
import ru.practicum.explorewithme.repository.UserRepository;
import ru.practicum.explorewithme.repository.specification.UsersEventSearchSpecifications;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl extends ServiceBase implements EventService {
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final RequestService requestService;
    private final StatsClient statsClient;
    private static final int USERS_MIN_HOURS_OFFSET = 2;
    private static final int ADMINS_MIN_HOURS_OFFSET = 1;

    @Override
    public List<EventDto> getEvents(long userId, int from, int size) {
        Pageable pageable = new OffsetPageRequest(from, size);
        List<Event> events = eventRepository.findByInitiatorId(userId, pageable);

        if (events.isEmpty()) {
            return List.of();
        }

        return getEventsWithStats(events, statsClient);
    }

    @Override
    @Transactional
    public EventDto createEvent(long userId, NewEventDto newEventDto) {
        checkTimeBeforeEventStart(newEventDto.getEventDate(), USERS_MIN_HOURS_OFFSET);
        Category category = findEntityIn(categoryRepository, newEventDto.getCategory());
        User initiator = findEntityIn(userRepository, userId);
        LocationEmbeddable locationEmbeddable = LocationMapper.toLocationEmbeddable(newEventDto.getLocation());
        //Формируем событие и сохраняем
        Event event = EventMapper.toEvent(newEventDto);
        setNestedClassesValues(event, category, initiator, locationEmbeddable);
        setParamsOnCreation(event);

        Event createdEvent = eventRepository.save(event);

        EventDto result = EventMapper.toEventDto(createdEvent);
        CategoryDto categoryDto = CategoryMapper.toCategoryDto(createdEvent.getCategory());
        UserShortDto userShortDto = UserMapper.toUserShortDto(createdEvent.getInitiator());
        Location location = LocationMapper.toLocation(createdEvent.getLocation());
        setNestedClassesValues(result, categoryDto, userShortDto, location);
        return result;
    }

    @Override
    public EventDto getEvent(long userId, long eventId) {
        Event event = getEventById(eventId, userId);
        return getEventsWithStats(List.of(event), statsClient).getFirst();
    }

    @Override
    @Transactional
    public EventDto updateEvent(long userId, long eventId, UserUpdateEventDto update) {
        if (update.getEventDate() != null) {
            checkTimeBeforeEventStart(update.getEventDate(), USERS_MIN_HOURS_OFFSET);
        }

        Event event = getEventById(eventId, userId);
        checkEventNotCanceled(event);
        EventStatus status = changeEventStatus(update);
        updateEventFields(event, update, status);
        Event updatedEvent = eventRepository.save(event);
        return getEventsWithStats(List.of(updatedEvent), statsClient).getFirst();
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
                .and(UsersEventSearchSpecifications.hasStatus(EventStatus.PUBLISHED))
                .and(UsersEventSearchSpecifications.textContains(text))
                .and(UsersEventSearchSpecifications.categoryIn(categories))
                .and(UsersEventSearchSpecifications.paidEquals(paid))
                .and(UsersEventSearchSpecifications.eventDateFrom(start))
                .and(UsersEventSearchSpecifications.eventDateTo(rangeEnd))
                .and(UsersEventSearchSpecifications.onlyAvailable(onlyAvailable));

        Sort eventsSort = sort == PublicEventSort.EVENT_DATE ? Sort.by("eventDate").ascending() : Sort.unsorted();
        List<Event> events = eventRepository.findAll(specification, eventsSort);

        if (events.isEmpty()) {
            return List.of();
        }

        List<EventDto> eventsDto = getEventsWithStats(events, statsClient);
        List<EventShortDto> dtos = eventsDto.stream()
                .map(EventMapper::toEventShortDto)
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
                .orElseThrow(() -> new IdNotFoundException(eventId));
        Map<String, Long> viewsByUri = getStats(statsClient, List.of(event));
        return getEventsWithStats(List.of(event), statsClient).getFirst();
    }

    @Override
    public Event getEventById(long eventId, long userId) {
        return eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new IdNotFoundException(eventId));
    }

    @Override
    public List<EventDto> searchEvents(List<Long> users,
                                       List<String> states,
                                       List<Long> categories,
                                       String rangeStart,
                                       String rangeEnd,
                                       int from,
                                       int size) {
        Specification<Event> spec = new AdminEventSearchSpecification(users, states, categories, rangeStart, rangeEnd);
        Pageable pageable = new OffsetPageRequest(from, size);
        Page<Event> eventPage = eventRepository.findAll(spec, pageable);
        List<Event> events = eventPage.getContent();

        if (events.isEmpty()) {
            return List.of();
        }

        return getEventsWithStats(events, statsClient);
    }

    @Override
    @Transactional
    public EventDto updateEvent(long eventId, AdminUpdateEventDto update) {
        if (update.getEventDate() != null) {
            checkTimeBeforeEventStart(update.getEventDate(), ADMINS_MIN_HOURS_OFFSET);
        }

        Event event = findEntityIn(eventRepository, eventId);
        checkEventNotPublished(event);
        EventStatus status = changeEventStatus(event, update);
        updateEventFields(event, update, status);
        Event updatedEvent = eventRepository.save(event);
        return getEventsWithStats(List.of(updatedEvent), statsClient).getFirst();
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
                .app(ExploreWithMeMainService.APP_NAME)
                .uri(uri)
                .ip(ip)
                .timestamp(LocalDateTime.now())
                .build());
    }

    private void checkDateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new WrongDateIntervalException("rangeStart must be before rangeEnd");
        }
    }

    private void updateEventFields(Event event, EventUpdateCommon update, EventStatus status) {
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
            LocationEmbeddable location = LocationMapper.toLocationEmbeddable(update.getLocation());
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
            throw new IdNotFoundException(userId);
        }
    }

    private void checkTimeBeforeEventStart(LocalDateTime eventDate, int minOffset) {
        if (!eventDate.isAfter(LocalDateTime.now().plusHours(minOffset))) {
            throw new EarlyDateException(minOffset, eventDate);
        }
    }

    private void setParamsOnCreation(Event event) {
        event.setCreatedOn(LocalDateTime.now());
        event.setStatus(EventStatus.PENDING);
        event.setRequestModeration(true);
    }

    private void checkEventNotCanceled(Event event) {
        if (!event.getStatus().equals(EventStatus.CANCELED) && !event.getStatus().equals(EventStatus.PENDING)) {
            throw new UnavailableUpdateException("Event", event.getId());
        }
    }

    private void checkEventNotPublished(Event event) {
        if (!event.getStatus().equals(EventStatus.PENDING)) {
            throw new UnavailableUpdateException("Event", event.getId());
        }
    }

    private EventStatus changeEventStatus(UserUpdateEventDto update) {
        EventStatus status;

        if (update.getStatus() != null && update.getStatus().equals(UserEventUpdateAction.CANCEL_REVIEW)) {
            status = EventStatus.CANCELED;
        } else {
            status = EventStatus.PENDING;
        }

        return status;
    }

    private EventStatus changeEventStatus(Event event, AdminUpdateEventDto update) {
        EventStatus status;

        if (update.getStatus() != null && update.getStatus().equals(AdminEventUpdateAction.PUBLISH_EVENT)) {
            status = EventStatus.PUBLISHED;
        } else if (update.getStatus() != null && update.getStatus().equals(AdminEventUpdateAction.REJECT_EVENT)) {
            status = EventStatus.CANCELED;
        } else {
            status = event.getStatus();
        }

        return status;
    }
}
