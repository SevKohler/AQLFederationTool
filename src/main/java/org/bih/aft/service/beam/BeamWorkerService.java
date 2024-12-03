package org.bih.aft.service.beam;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bih.aft.config.BeamProperties;
import org.bih.aft.controller.dao.AQLinput;
import org.bih.aft.service.query.OpenEhrQueryService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@ConditionalOnProperty(value = "aft.protocol", havingValue = "BEAM")
@Service
@RequiredArgsConstructor
@Slf4j
public class BeamWorkerService {

    private final BeamProperties properties;
    private final OpenEhrQueryService openEhrQueryService;

    private final ParameterizedTypeReference<List<Task>> resultType = new ParameterizedTypeReference<>() {};

    private RestClient restClient;

    @PostConstruct
    public void init() {
        restClient = RestClient.builder()
                .baseUrl(properties.getProxyUrl().toString())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "ApiKey %s %s".formatted(properties.getAppId(), properties.getAppSecret()))
                .build();
    }

    @Scheduled(fixedDelay = 1)
    public void handleTasks() {
        try {
            ResponseEntity<List<Task>> tasks = restClient.get()
                    .uri("/v1/tasks?wait_count={waitCount}&filter=todo", "1")
                    .retrieve()
                    .toEntity(resultType);

            tasks.getBody().forEach(this::executeTask);
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    void executeTask(Task task) {
        Result result;
        try {
            var number = openEhrQueryService.executeCountQuery(new AQLinput(task.body()));
            result = Result.fromTask(
                    task,
                    properties.getAppId(),
                    Status.succeeded,
                    number
            );
        } catch (Exception e) {
            result = Result.fromTask(
                    task,
                    properties.getAppId(),
                    Status.permfailed,
                    ""
            );
        }

        restClient.put()
                .uri("/v1/tasks/{task_id}/results/{app_id}", task.id(), properties.getAppId())
                .body(result)
                .retrieve();
    }
}
