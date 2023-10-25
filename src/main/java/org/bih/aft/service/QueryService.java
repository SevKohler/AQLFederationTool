package org.bih.aft.service;

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

import java.util.ArrayList;
import java.util.List;

public class QueryService {
    private final OpenEhrClient openEhrClient;

    public QueryService(OpenEhrClient openEhrClient) {
        this.openEhrClient = openEhrClient;
    }

    public FeasabilityOutput query(AQLinput aqlQuery) throws InvalidCountQuery {
        validateQueryForCount(aqlQuery);
        return generateFeasabilityOutput(executeAqlQuery(aqlQuery.getAql()));
    }

    private FeasabilityOutput generateFeasabilityOutput(List<Record1<String>> aqlResult) {
        FeasabilityOutput feasabilityOutput = new FeasabilityOutput();
        if(Integer.parseInt(aqlResult.get(0).value1())>10){
            feasabilityOutput.setLocation("Charite");
            feasabilityOutput.setPatients(aqlResult.get(0).value1());
        }else{
            feasabilityOutput.setLocation("Charite");
            feasabilityOutput.setPatients("NA");
        }
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
        List<SelectExpression> selectExpressions= selectClause.getStatement();
        try{
            AggregateFunction aggregateFunction = (AggregateFunction) selectExpressions.get(0).getColumnExpression();
            if(!aggregateFunction.getFunctionName().equals(AggregateFunction.AggregateFunctionName.COUNT)) {
                throw new InvalidCountQuery("Function has to be COUNT");
            }
        }catch (ClassCastException e){
            throw new InvalidCountQuery("No COUNT included in Select statement");
        }
    }

    private void checkSize(SelectClause selectClause) {
        List<SelectExpression> selectExpressions= selectClause.getStatement();
        if(selectExpressions.size()>1) {
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
