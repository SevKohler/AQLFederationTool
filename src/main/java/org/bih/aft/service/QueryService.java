package org.bih.aft.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bih.aft.Exceptions.InvalidCountQuery;
import org.bih.aft.controller.dao.AQLinput;
import org.bih.aft.service.dao.FeasabilityOutput;
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
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class QueryService {
    private final OpenEhrClient openEhrClient;
    private static final Logger LOG = LoggerFactory.getLogger(QueryService.class);

    public QueryService(OpenEhrClient openEhrClient) {
        this.openEhrClient = openEhrClient;
    }

    public List<FeasabilityOutput> federateQuery(AQLinput aqlQuery) throws InvalidCountQuery {
        validateQueryForCount(aqlQuery);
        LOG.info("Query validated");
        FeasabilityOutput feasabilityOutput = federate(aqlQuery);
        return generateFeasabilityOutput(feasabilityOutput, executeAqlQuery(aqlQuery.getAql()));
    }

    public List<FeasabilityOutput> localQuery(AQLinput aqlQuery) throws InvalidCountQuery {
        validateQueryForCount(aqlQuery);
        LOG.info("Query validated");
        return generateFeasabilityOutput(executeAqlQuery(aqlQuery.getAql()));
    }

    private FeasabilityOutput federate(AQLinput aqlQuery) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        JSONObject aql = new JSONObject();
        aql.put("aql", aqlQuery.getAql());
        final String uri = "http://localhost:8091/query/local";
        HttpEntity request =
                new HttpEntity(aql.toString(), headers);
        RestTemplate restTemplate = new RestTemplate();
        String result = restTemplate.postForObject(uri, request, String.class);
        LOG.info("Query federated");
        FeasabilityOutput feasabilityOutput = new FeasabilityOutput();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            feasabilityOutput = objectMapper.readValue(result, FeasabilityOutput.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return feasabilityOutput;
    }


    private List<FeasabilityOutput> generateFeasabilityOutput(FeasabilityOutput feasabilityOutputFederated, List<Record1<String>> aqlResult) {
        LOG.info("Query executed");
        FeasabilityOutput feasabilityOutput = new FeasabilityOutput();
        if (Integer.parseInt(aqlResult.get(0).value1()) > 10) {
            feasabilityOutput.setLocation("Charite");
            feasabilityOutput.setPatients(aqlResult.get(0).value1());
        } else {
            feasabilityOutput.setLocation("Charite");
            feasabilityOutput.setPatients("NA");
        }
        List<FeasabilityOutput> federatedResults = new ArrayList<>();
        federatedResults.add(feasabilityOutputFederated);
        federatedResults.add(feasabilityOutput);
        LOG.info("Query finalized");
        return federatedResults;
    }

    private List<FeasabilityOutput> generateFeasabilityOutput(List<Record1<String>> aqlResult) {
        LOG.info("Query executed");
        FeasabilityOutput feasabilityOutput = new FeasabilityOutput();
        if (Integer.parseInt(aqlResult.get(0).value1()) > 10) {
            feasabilityOutput.setLocation("Charite");
            feasabilityOutput.setPatients(aqlResult.get(0).value1());
        } else {
            feasabilityOutput.setLocation("Charite");
            feasabilityOutput.setPatients("NA");
        }
        LOG.info("Query finalized");
        return List.of(feasabilityOutput);
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
}
