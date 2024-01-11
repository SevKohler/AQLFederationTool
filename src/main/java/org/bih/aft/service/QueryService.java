package org.bih.aft.service;

import org.bih.aft.controller.dao.AQLinput;
import org.bih.aft.exceptions.InvalidCountQuery;
import org.bih.aft.ports.QueryUseCase;
import org.bih.aft.service.dao.FeasabilityOutput;
import org.bih.aft.service.dao.Location;
import org.ehrbase.openehr.sdk.aql.dto.AqlQuery;
import org.ehrbase.openehr.sdk.aql.dto.operand.AggregateFunction;
import org.ehrbase.openehr.sdk.aql.dto.select.SelectClause;
import org.ehrbase.openehr.sdk.aql.dto.select.SelectExpression;
import org.ehrbase.openehr.sdk.client.openehrclient.OpenEhrClient;
import org.ehrbase.openehr.sdk.generator.commons.aql.query.NativeQuery;
import org.ehrbase.openehr.sdk.generator.commons.aql.query.Query;
import org.ehrbase.openehr.sdk.generator.commons.aql.record.Record1;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class QueryService implements QueryUseCase {

    @Value("${aft.location}")
    private String homeLocation;

    private final OpenEhrClient openEhrClient;

    private final FederationListService federationListService;


    private static final Logger LOG = LoggerFactory.getLogger(QueryService.class);

    public QueryService(OpenEhrClient openEhrClient, FederationListService federationListService) {
        this.openEhrClient = openEhrClient;
        this.federationListService = federationListService;
    }



    private List<FeasabilityOutput> federate2(AQLinput aqlQuery) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject aql = new JSONObject();
        aql.put("aql", aqlQuery.getAql());
        HttpEntity request = new HttpEntity(aql.toString(), headers);
        RestTemplate restTemplate = new RestTemplate();
        List<FeasabilityOutput> feasabilityOutputList = new ArrayList<>();
        for (Location location : federationListService.getFederationList().locations()) {
            sendQueryToLocation(location, restTemplate, request, feasabilityOutputList);
        }
        LOG.info("Query federated");
        return feasabilityOutputList;
    }

    private void sendQueryToLocation(Location location, RestTemplate restTemplate, HttpEntity request, List<FeasabilityOutput> feasabilityOutputList) {
        try {
            final String uri = location.url() + "/query/local";
            ResponseEntity<FeasabilityOutput> result = restTemplate.postForEntity(uri, request, FeasabilityOutput.class);
            feasabilityOutputList.add(result.getBody());
        } catch (ResourceAccessException e) {
            LOG.warn("Location " + location.name() + " could not be reached. Error: " + e);
            FeasabilityOutput feasabilityOutput = new FeasabilityOutput();
            feasabilityOutput.setLocation(location.name());
            feasabilityOutput.setPatients("Error");
            feasabilityOutputList.add(feasabilityOutput);
        }
    }


    private List<FeasabilityOutput> generateFeasabilityOutput(List<FeasabilityOutput> feasabilityOutputFederated, List<Record1<String>> aqlResult) {
        feasabilityOutputFederated.add(processQueryLocally(aqlResult));
        LOG.info("Query finalized");
        return feasabilityOutputFederated;
    }

    private FeasabilityOutput processQueryLocally(List<Record1<String>> aqlResult) {
        LOG.info("Query executed");
        FeasabilityOutput feasabilityOutput = new FeasabilityOutput();
        if (Integer.parseInt(aqlResult.get(0).value1()) > 10) {
            feasabilityOutput.setLocation(homeLocation);
            feasabilityOutput.setPatients(aqlResult.get(0).value1());
        } else {
            feasabilityOutput.setLocation(homeLocation);
            feasabilityOutput.setPatients("NA");
        }
        LOG.info("Query finalized");
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
    public List<FeasabilityOutput> federate(AQLinput aqlQuery) throws InvalidCountQuery {
        validateQueryForCount(aqlQuery);
        LOG.info("Query validated");
        List<FeasabilityOutput> feasabilityOutput = federate2(aqlQuery);
        return generateFeasabilityOutput(feasabilityOutput, executeAqlQuery(aqlQuery.getAql()));
    }

    @Override
    public FeasabilityOutput local(AQLinput aqlQuery) throws InvalidCountQuery {
        validateQueryForCount(aqlQuery);
        LOG.info("Query validated");
        return processQueryLocally(executeAqlQuery(aqlQuery.getAql()));
    }
}
