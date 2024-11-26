package org.bih.aft.service.beam;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bih.aft.config.AftProperties;
import org.bih.aft.config.BeamProperties;
import org.bih.aft.controller.dao.AQLinput;
import org.bih.aft.ports.QueryUseCase;
import org.bih.aft.service.FederationListService;
import org.bih.aft.service.dao.FeasibilityOutput;
import org.bih.aft.service.dao.Location;
import org.bih.aft.service.query.OpenEhrQueryService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@ConditionalOnProperty(value = "aft.protocol", havingValue = "BEAM")
@Service
@RequiredArgsConstructor
@Slf4j
public class BeamFederationService implements QueryUseCase {

    private final AftProperties aftProperties;
    private final BeamProperties properties;
    private final FederationListService federationListService;
    private final OpenEhrQueryService openEhrQueryService;

    private final int waitTime = 20;
    private final ParameterizedTypeReference<List<Result>> resultType = new ParameterizedTypeReference<>() {};
    private final ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
            .withReadTimeout(Duration.ofSeconds(waitTime+1));

    private RestClient restClient;
    private List<Location> locations;
    private Map<String, String> urlToName;

    @PostConstruct
    public void init() {
        restClient = RestClient.builder()
            .baseUrl(properties.getProxyUrl().toString())
            .requestFactory(ClientHttpRequestFactories.get(settings))
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "ApiKey %s %s".formatted(properties.getAppId(), properties.getAppSecret()))
            .build();

        locations = federationListService.locations();

        urlToName = locations
                .stream()
                .collect(Collectors.toMap(Location::url, Location::name));
    }

    @Override
    public List<FeasibilityOutput> federate(AQLinput query) {
        Task task = new Task(
                UUID.randomUUID().toString(),
                properties.getAppId(),
                locations.stream().map(Location::url).toList(),
                query.aql(),
                Discard.discard,
                "%ss".formatted(waitTime),
                Map.of()
        );

        URI location = restClient.post()
                .uri( "/v1/tasks")
                .body(task)
                .retrieve()
                .toBodilessEntity()
                .getHeaders()
                .getLocation();

        ResponseEntity<List<Result>> results = restClient.get()
                .uri(location.getPath() + "/results?wait_count={size}&wait_time={waitTime}", locations.size(), "%ss".formatted(waitTime))
                .retrieve()
                .toEntity(resultType);

        var res = locations.stream().collect(Collectors.toMap(Location::name, e -> "?"));
        res.put(aftProperties.getLocation(), openEhrQueryService.executeCountQuery(new AQLinput(query.aql())));
        res.putAll(results.getBody().stream().collect(Collectors.toMap(e -> urlToName.get(e.from()), Result::body)));

        return res.entrySet().stream()
                .map(e -> new FeasibilityOutput(e.getKey(), e.getValue()))
                .toList();
    }

    @Override
    public FeasibilityOutput local(AQLinput query) {
        throw new UnsupportedOperationException("Not supported in Beam mode.");
    }
}
