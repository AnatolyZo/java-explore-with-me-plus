package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.repository.EndpointHitRepository;
import ru.practicum.explorewithme.stats.ViewStatsResponse;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ViewStatsServiceImpl implements ViewStatsService {
    private final EndpointHitRepository endpointHitRepository;

    @Override
    public List<ViewStatsResponse> getStatistics(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (uris == null || uris.isEmpty()) {
            if (unique) {
                return endpointHitRepository.findUniqueStatsByDate(start, end);
            } else {
                return endpointHitRepository.findStatsByDate(start, end);
            }
        }
        if (unique) {
            return endpointHitRepository.findUniqueStatsByDateAndUris(start, end, uris);
        }
        return endpointHitRepository.findStatsByDateAndUris(start, end, uris);
    }
}
