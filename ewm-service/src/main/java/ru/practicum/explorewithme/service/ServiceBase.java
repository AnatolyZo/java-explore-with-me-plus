package ru.practicum.explorewithme.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.explorewithme.StatsClient;
import ru.practicum.explorewithme.dto.EventDto;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.exception.IdNotFoundException;
import ru.practicum.explorewithme.mapper.EventMapper;
import ru.practicum.explorewithme.stats.ViewStatsResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.practicum.explorewithme.controller.ControllerConstants.URL_EVENTS;

public class ServiceBase {
    /// Убрал первый параметр из NFE чтобы не усложнять метод
    protected <E> E findEntityIn(JpaRepository<E, Long> repository, long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IdNotFoundException(id));
    }

    protected <E> void checkEntityExistsIn(JpaRepository<E, Long> repository, long id) {
        if (!repository.existsById(id)) {
            throw new IdNotFoundException(id);
        }
    }

    protected Map<String, Long> getStats(StatsClient statsClient, List<Event> events) {
        /// Удобней мапить по обьекту-ивенту(ну или хотя бы по id), а не строке.
        //Определяем самую раннюю дату создания события пользователем
        LocalDateTime start = events.stream()
                .map(Event::getCreatedOn)
                .min(LocalDateTime::compareTo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No events were found"));
        LocalDateTime end = LocalDateTime.now();

        //Получаем список uri для отправки в сервер статистики
        List<String> uris = events.stream()
                .map(event -> URL_EVENTS + "/" + event.getId())
                .toList();

        List<ViewStatsResponse> stats = statsClient.getStatistics(start, end, uris, false);
        Map<String, Long> viewsByUri = new HashMap<>();
        stats.forEach(stat -> viewsByUri.put(stat.getUri(), stat.getHits()));

        return viewsByUri;
    }

    protected List<EventDto> getEventsWithStats(List<Event> events, Map<String, Long> viewsByUri) {
        return events.stream()
                .map(event -> EventMapper.mapToEventDto(event, getViews(event, viewsByUri)))
                .toList();
    }

    protected long getViews(Event event, Map<String, Long> viewsByUri) {
        return viewsByUri.getOrDefault(URL_EVENTS + "/" + event.getId(), 0L);
    }
}
