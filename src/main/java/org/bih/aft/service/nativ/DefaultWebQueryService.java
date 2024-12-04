package org.bih.aft.service.nativ;

import lombok.extern.slf4j.Slf4j;
import org.bih.aft.controller.dao.AqlWithParams;
import org.bih.aft.service.dao.FeasibilityOutput;
import org.bih.aft.service.dao.Location;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.concurrent.CompletableFuture;

@ConditionalOnProperty(value = "aft.protocol", havingValue = "NATIVE")
@Slf4j
@Service
public class DefaultWebQueryService implements QueryService {

    private final RestClient restClient = RestClient.create();

    @Override
    @Async
    public CompletableFuture<FeasibilityOutput> sendQuery(Location location, AqlWithParams aqlQuery) {
        try {
            var result = restClient.post()
                    .uri(localQueryEndpoint(location.url()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(aqlQuery)
                    .retrieve()
                    .toEntity(FeasibilityOutput.class);
            return CompletableFuture.completedFuture(new FeasibilityOutput(location.name(), result.getBody().getPatients()));
        } catch (ResourceAccessException e) {
            log.warn("Location " + location.name() + " could not be reached. Error: " + e);
            return CompletableFuture.completedFuture(new FeasibilityOutput(location.name(), "Error"));
        }
    }

    private String localQueryEndpoint(String url) {
        return url + "/query/local";
    }
}
