package org.bih.aft.service;

import lombok.extern.slf4j.Slf4j;
import org.bih.aft.controller.dao.AQLinput;
import org.bih.aft.exceptions.InvalidCountQuery;
import org.ehrbase.openehr.sdk.aql.dto.AqlQuery;
import org.ehrbase.openehr.sdk.aql.dto.operand.AggregateFunction;
import org.ehrbase.openehr.sdk.aql.dto.select.SelectClause;
import org.ehrbase.openehr.sdk.aql.dto.select.SelectExpression;

import java.util.List;
@Slf4j
public class CountVerificator implements QueryVerificator {
    @Override
    public void verify(AQLinput aqlQuery) { // TODO: What return type here ? Response should be 400 in the end
        String query = aqlQuery.aql();
        AqlQuery aqlQuery1 = AqlQuery.parse(query);
        SelectClause selectClause = aqlQuery1.getSelect();
        checkSize(selectClause);
        checkCount(selectClause);
        log.info("Query verified");
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
}
