package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.entity.EndpointHit;
import ru.practicum.explorewithme.hit.EndpointHitRequest;
import ru.practicum.explorewithme.repository.EndpointHitRepository;
import ru.practicum.explorewithme.service.mapper.EndpointHitMapper;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EndpointHitServiceImpl implements EndpointHitService {
    private final EndpointHitMapper endpointHitMapper;
    private final EndpointHitRepository endpointHitRepository;

    @Override
    @Transactional
    public void saveHit(EndpointHitRequest request) {
        EndpointHit endpointHit = endpointHitMapper.toEndpointHit(request);
        endpointHitRepository.save(endpointHit);
    }
}
