package org.bih.aft.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bih.aft.controller.dao.AqlWithParams;
import org.ehrbase.openehr.sdk.client.openehrclient.OpenEhrClient;
import org.ehrbase.openehr.sdk.generator.commons.aql.parameter.ParameterValue;
import org.ehrbase.openehr.sdk.generator.commons.aql.query.NativeQuery;
import org.ehrbase.openehr.sdk.generator.commons.aql.query.Query;
import org.ehrbase.openehr.sdk.generator.commons.aql.record.Record1;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
class QueryService {

    private final OpenEhrClient openEhrClient;

    long executeCountQuery(AqlWithParams inputQuery) {
        NativeQuery<Record1<Long>> query = Query.buildNativeQuery(inputQuery.aql(), Long.class);
        try {
            return openEhrClient.aqlEndpoint().execute(query, inputQuery.parameters().entrySet().stream().map(e-> new ParameterValue<>(e.getKey(), e.getValue())).toArray(ParameterValue[]::new)).get(0).value1();
        } catch (NullPointerException nullPointerException) {
            return 0L; // Some platform return a NullPointer if nothing is found.
        }
    }
}
