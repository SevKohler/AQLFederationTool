package org.bih.aft.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bih.aft.controller.dao.AQLinput;
import org.bih.aft.exceptions.InvalidCountQuery;
import org.bih.aft.ports.QueryUseCase;
import org.bih.aft.service.dao.FeasibilityOutput;
import org.bih.aft.service.dao.Location;
import org.ehrbase.openehr.sdk.aql.dto.AqlQuery;
import org.ehrbase.openehr.sdk.aql.dto.operand.AggregateFunction;
import org.ehrbase.openehr.sdk.aql.dto.select.SelectClause;
import org.ehrbase.openehr.sdk.aql.dto.select.SelectExpression;
import org.ehrbase.openehr.sdk.client.openehrclient.OpenEhrClient;
import org.ehrbase.openehr.sdk.generator.commons.aql.query.NativeQuery;
import org.ehrbase.openehr.sdk.generator.commons.aql.query.Query;
import org.ehrbase.openehr.sdk.generator.commons.aql.record.Record1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    OpenEhrClient openEhrClient;

     LocationProvider federationListService;

    QueryService queryService;

    private List<FeasibilityOutput> federate2(AQLinput aqlQuery) {
        List<FeasibilityOutput> feasabilityOutputList = new ArrayList<>();
        for (Location location : federationListService.locations()) {
            feasabilityOutputList.add(queryService.sendQuery(location, aqlQuery));
        }
        log.info("Query federated");
        return feasabilityOutputList;
    }

    private List<FeasibilityOutput> generateFeasabilityOutput(List<FeasibilityOutput> feasabilityOutputFederated, List<Record1<String>> aqlResult) {
        feasabilityOutputFederated.add(processQueryLocally(aqlResult));
        log.info("Query finalized");
        return feasabilityOutputFederated;
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


    private void validateQueryForCount(AQLinput aqlQuery) {
        String query = aqlQuery.getAql();
        openEhrClient.ehrEndpoint();
        AqlQuery aqlQuery1 = AqlQuery.parse(query);
        SelectClause selectClause = aqlQuery1.getSelect();
        checkSize(selectClause);
        checkCount(selectClause);
        executeAqlQuery(query);
    }

    private void checkCount(SelectClause selectClause) {
        List<SelectExpression> selectExpressions = selectClause.getStatement();
        try {
            AggregateFunction aggregateFunction = (AggregateFunction) selectExpressions.get(0).getColumnExpression();
            if (!aggregateFunction.getFunctionName().equals(AggregateFunction.AggregateFunctionName.COUNT)) {
                throw new InvalidCountQuery("Function has to be COUNT");
            }
        } catch (ClassCastException e) {
            throw new InvalidCountQuery("No COUNT included in Select statement");
        }
    }

    private void checkSize(SelectClause selectClause) {
        List<SelectExpression> selectExpressions = selectClause.getStatement();
        if (selectExpressions.size() > 1) {
            throw new InvalidCountQuery("Only one Select clause with one statement is allowed");
        }
    }

    private List<Record1<String>> executeAqlQuery(String inputQuery) {
        NativeQuery<Record1<String>> query = Query.buildNativeQuery(inputQuery, String.class);
        try {
            return openEhrClient.aqlEndpoint().execute(query);
        } catch (NullPointerException nullPointerException) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<FeasibilityOutput> federate(AQLinput aqlQuery) throws InvalidCountQuery {
        validateQueryForCount(aqlQuery);
        log.info("Query validated");
        List<FeasibilityOutput> feasabilityOutput = federate2(aqlQuery);
        return generateFeasabilityOutput(feasabilityOutput, executeAqlQuery(aqlQuery.getAql()));
    }

    @Override
    public FeasibilityOutput local(AQLinput aqlQuery) throws InvalidCountQuery {
        validateQueryForCount(aqlQuery);
        log.info("Query validated");
        return processQueryLocally(executeAqlQuery(aqlQuery.getAql()));
    }
}
