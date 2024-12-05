package org.bih.aft.service.query;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bih.aft.config.AftProperties;
import org.bih.aft.controller.dao.AQLinput;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class OpenEhrQueryService {

    private final AftProperties properties;
    private final List<QueryVerificator> queryVerificators;
    private final QueryService queryService;

    public String executeCountQuery(AQLinput aql) {
        queryVerificators.forEach(queryVerificator -> {
            queryVerificator.verify(aql);
        });

        var result = queryService.executeCountQuery(aql.aql());

        if (result > properties.getMinHits()) {
            return Long.toString(result);
        } else {
            return "NA";
        }
    }
}
