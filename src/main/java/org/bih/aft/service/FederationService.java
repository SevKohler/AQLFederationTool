package org.bih.aft.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bih.aft.controller.dao.AQLinput;
import org.bih.aft.exceptions.InvalidCountQuery;
import org.bih.aft.ports.QueryUseCase;
import org.bih.aft.service.dao.FeasibilityOutput;
import org.bih.aft.service.dao.Location;
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

    private final QueryVerificator queryVerificator;

    private final LocationProvider federationListService;

    private final QueryService queryService;

    @Override
    public List<FeasibilityOutput> federate(AQLinput aqlQuery) throws InvalidCountQuery {
        queryVerificator.verify(aqlQuery);
        List<FeasibilityOutput> feasabilityOutput = processQueryFederated(aqlQuery);
        return generateFeasabilityOutput(feasabilityOutput, openEhrQueryService.executeCountQuery(aqlQuery.aql()));

    }

    @Override
    public FeasibilityOutput local(AQLinput aqlQuery) throws InvalidCountQuery {
        queryVerificator.verify(aqlQuery);
        return processQueryLocally(openEhrQueryService.executeCountQuery(aqlQuery.aql()));
    }

    private List<FeasibilityOutput> processQueryFederated(AQLinput aqlQuery) {
        List<FeasibilityOutput> feasabilityOutputList = new ArrayList<>();
        for (Location location : federationListService.locations()) {
            feasabilityOutputList.add(queryService.sendQuery(location, aqlQuery));
        }
        log.info("Query federated");
        return feasabilityOutputList;
    }

    private FeasibilityOutput processQueryLocally(long aqlResult) {
        log.info("Query executed");
        FeasibilityOutput feasabilityOutput = new FeasibilityOutput();
        if (aqlResult > 10) {
            feasabilityOutput.setLocation(homeLocation);
            feasabilityOutput.setPatients(Long.toString(aqlResult));
        } else {
            feasabilityOutput.setLocation(homeLocation);
            feasabilityOutput.setPatients("NA");
        }
        log.info("Query finalized");
        return feasabilityOutput;
    }

    private List<FeasibilityOutput> generateFeasabilityOutput(List<FeasibilityOutput> feasabilityOutputFederated, long aqlResult) {
        feasabilityOutputFederated.add(processQueryLocally(aqlResult));
        log.info("Query finalized");
        return feasabilityOutputFederated;
    }


}
