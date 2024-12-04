package org.bih.aft.service.nativ;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bih.aft.controller.dao.AqlWithParams;
import org.bih.aft.exceptions.InvalidCountQuery;
import org.bih.aft.ports.QueryUseCase;
import org.bih.aft.service.LocationProvider;
import org.bih.aft.service.dao.FeasibilityOutput;
import org.bih.aft.service.dao.Location;
import org.bih.aft.service.query.OpenEhrQueryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@ConditionalOnProperty(value = "aft.protocol", havingValue = "NATIVE")
@Service
@RequiredArgsConstructor
@Slf4j
public class FederationService implements QueryUseCase {

    @Value("${aft.location}")
    private String homeLocation;

    private final OpenEhrQueryService openEhrQueryService;

    private final LocationProvider federationListService;

    private final QueryService queryService;

    @Override
    public List<FeasibilityOutput> federate(AqlWithParams aqlQuery) throws InvalidCountQuery {
        List<FeasibilityOutput> feasabilityOutput = processQueryFederated(aqlQuery);
        feasabilityOutput.add(new FeasibilityOutput(homeLocation, openEhrQueryService.executeCountQuery(aqlQuery)));
        log.info("Query finalized");
        return feasabilityOutput;
    }

    @Override
    public FeasibilityOutput local(AqlWithParams aqlQuery) throws InvalidCountQuery {
        return new FeasibilityOutput(homeLocation, openEhrQueryService.executeCountQuery(aqlQuery));
    }

    private List<FeasibilityOutput> processQueryFederated(AqlWithParams aqlQuery) {
        List<CompletableFuture<FeasibilityOutput>> feasabilityOutputList = new ArrayList<>();
        for (Location location : federationListService.locations()) {
            feasabilityOutputList.add(queryService.sendQuery(location, aqlQuery));
        }
        CompletableFuture.allOf(feasabilityOutputList.toArray(new CompletableFuture[0])).join();
        log.info("Query federated");
        return feasabilityOutputList.stream().map(CompletableFuture::join).toList();
    }
}
