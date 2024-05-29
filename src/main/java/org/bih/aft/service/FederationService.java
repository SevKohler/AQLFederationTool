package org.bih.aft.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bih.aft.controller.dao.AQLinput;
import org.bih.aft.exceptions.InvalidCountQuery;
import org.bih.aft.ports.QueryUseCase;
import org.bih.aft.service.dao.FeasibilityOutput;
import org.bih.aft.service.dao.Location;
import org.ehrbase.openehr.sdk.client.openehrclient.OpenEhrClient;
import org.ehrbase.openehr.sdk.generator.commons.aql.query.NativeQuery;
import org.ehrbase.openehr.sdk.generator.commons.aql.query.Query;
import org.ehrbase.openehr.sdk.generator.commons.aql.record.Record1;
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

    private final OpenEhrClient openEhrClient;

    private final QueryVerificator queryVerificator;

    private final LocationProvider federationListService;

    private final QueryService queryService;

    @Override
    public List<FeasibilityOutput> federate(AQLinput aqlQuery) throws InvalidCountQuery {
        queryVerificator.verify(aqlQuery);
        List<FeasibilityOutput> feasabilityOutput = processQueryFederated(aqlQuery);
        return generateFeasabilityOutput(feasabilityOutput, executeAqlQuery(aqlQuery.aql()));

    }

    @Override
    public FeasibilityOutput local(AQLinput aqlQuery) throws InvalidCountQuery {
        queryVerificator.verify(aqlQuery);
        return processQueryLocally(executeAqlQuery(aqlQuery.aql()));
    }

    private List<FeasibilityOutput> processQueryFederated(AQLinput aqlQuery) {
        List<FeasibilityOutput> feasabilityOutputList = new ArrayList<>();
        for (Location location : federationListService.locations()) {
            feasabilityOutputList.add(queryService.sendQuery(location, aqlQuery));
        }
        log.info("Query federated");
        return feasabilityOutputList;
    }

    private FeasibilityOutput processQueryLocally(List<Record1<String>> aqlResult) {
        log.info("Query executed");
        FeasibilityOutput feasabilityOutput = new FeasibilityOutput();
        if (Integer.parseInt(aqlResult.get(0).value1()) > 10) {
            feasabilityOutput.setLocation(homeLocation);
            feasabilityOutput.setPatients(aqlResult.get(0).value1());
        } else {
            feasabilityOutput.setLocation(homeLocation);
            feasabilityOutput.setPatients("NA");
        }
        log.info("Query finalized");
        return feasabilityOutput;
    }

    private List<Record1<String>> executeAqlQuery(String inputQuery) {
        NativeQuery<Record1<String>> query = Query.buildNativeQuery(inputQuery, String.class);
        try {
            return openEhrClient.aqlEndpoint().execute(query);
        } catch (NullPointerException nullPointerException) {
            return new ArrayList<>(); // Some platform return a NullPointer if nothing is found.
        }
    }

    private List<FeasibilityOutput> generateFeasabilityOutput(List<FeasibilityOutput> feasabilityOutputFederated, List<Record1<String>> aqlResult) {
        feasabilityOutputFederated.add(processQueryLocally(aqlResult));
        log.info("Query finalized");
        return feasabilityOutputFederated;
    }


}
