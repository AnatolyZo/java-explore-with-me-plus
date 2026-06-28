package ru.practicum.explorewithme;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.explorewithme.hit.EndpointHitRequest;
import ru.practicum.explorewithme.stats.ViewStatsResponse;

@Slf4j
public class StatsClient {
    private final RestTemplate restTemplate;
    private static final String API_PREFIX_HIT = "/hit";
    private static final String API_PREFIX_STATS = "/stats";

    public StatsClient(String serverUrl, RestTemplateBuilder builder) {
        this.restTemplate = builder
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    public void addStatistics(EndpointHitRequest endpointHitRequest) {
        log.trace("Отправлен запрос на добавление статистических данных {}", endpointHitRequest);
        restTemplate.postForEntity(API_PREFIX_HIT, endpointHitRequest, Void.class);
    }

    public ViewStatsResponse getStatistics() {
        log.trace("Отправлен запрос на получение статистических данных");
        return restTemplate.getForObject(API_PREFIX_STATS, ViewStatsResponse.class);
    }
}
