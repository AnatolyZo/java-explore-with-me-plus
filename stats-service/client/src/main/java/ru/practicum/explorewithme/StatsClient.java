package ru.practicum.explorewithme;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

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

    public StatDtoResponse addStatistics(StatDtoRequest statDtoRequest) {
        return restTemplate.postForObject(API_PREFIX_HIT, statDtoRequest, StatDtoResponse.class);
    }

    public StatDtoResponse getStatistics() {
        return restTemplate.getForObject(API_PREFIX_STATS, StatDtoResponse.class);
    }
}
