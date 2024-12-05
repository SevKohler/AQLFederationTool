package org.bih.aft.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bih.aft.controller.dao.AQLinput;
import org.bih.aft.exceptions.InvalidCountQuery;
import org.bih.aft.ports.QueryUseCase;
import org.bih.aft.service.dao.FeasibilityOutput;
import org.bih.aft.service.dao.Location;
import org.bih.aft.service.query.OpenEhrQueryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
    public List<FeasibilityOutput> federate(AQLinput aqlQuery) throws InvalidCountQuery {
        List<FeasibilityOutput> feasabilityOutput = processQueryFederated(aqlQuery);
        feasabilityOutput.add(new FeasibilityOutput(homeLocation, openEhrQueryService.executeCountQuery(aqlQuery)));
        log.info("Query finalized");
        return feasabilityOutput;
    }

    @Override
    public FeasibilityOutput local(AQLinput aqlQuery) throws InvalidCountQuery {
        return new FeasibilityOutput(homeLocation, openEhrQueryService.executeCountQuery(aqlQuery));
    }

    private List<FeasibilityOutput> processQueryFederated(AQLinput aqlQuery) {
        List<FeasibilityOutput> feasabilityOutputList = new ArrayList<>();
        for (Location location : federationListService.locations()) {
            feasabilityOutputList.add(queryService.sendQuery(location, aqlQuery));
        }
        log.info("Query federated");
        return feasabilityOutputList;
    }
}
