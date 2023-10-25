package org.bih.aft.service;

import org.bih.aft.controller.dao.AQLinput;
import org.ehrbase.client.openehrclient.OpenEhrClient;
import org.ehrbase.openehr.sdk.aql.dto.AqlQuery;
import org.ehrbase.openehr.sdk.aql.dto.select.SelectClause;
import org.ehrbase.openehr.sdk.aql.dto.select.SelectExpression;

import java.util.List;

public class QueryService {
    private final OpenEhrClient openEhrClient;

    public QueryService(OpenEhrClient openEhrClient) {
        this.openEhrClient = openEhrClient;
    }

    public String query(AQLinput aqlQuery) {
        validateQueryForCount(aqlQuery);
        return null;
    }

    private void validateQueryForCount(AQLinput aqlQuery) {
        String query = aqlQuery.getAql();
        AqlQuery aqlQuery1 = AqlQuery.parse(query);
        SelectClause selectClause = aqlQuery1.getSelect();
        List<SelectExpression> selectExpressions= selectClause.getStatement();
        System.out.println(selectExpressions.getFirst());
    }
}
