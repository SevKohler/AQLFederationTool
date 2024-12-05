package org.bih.aft.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ehrbase.openehr.sdk.client.openehrclient.OpenEhrClient;
import org.ehrbase.openehr.sdk.generator.commons.aql.query.NativeQuery;
import org.ehrbase.openehr.sdk.generator.commons.aql.query.Query;
import org.ehrbase.openehr.sdk.generator.commons.aql.record.Record1;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
class QueryService {

    private final OpenEhrClient openEhrClient;

    long executeCountQuery(String inputQuery) {
        NativeQuery<Record1<Long>> query = Query.buildNativeQuery(inputQuery, Long.class);
        try {
            return openEhrClient.aqlEndpoint().execute(query).get(0).value1();
        } catch (NullPointerException nullPointerException) {
            return 0L; // Some platform return a NullPointer if nothing is found.
        }
    }
}
