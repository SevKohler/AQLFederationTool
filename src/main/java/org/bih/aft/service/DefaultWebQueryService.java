package org.bih.aft.service;

import lombok.extern.slf4j.Slf4j;
import org.bih.aft.controller.dao.AQLinput;
import org.bih.aft.service.dao.FeasibilityOutput;
import org.bih.aft.service.dao.Location;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class DefaultWebQueryService implements QueryService {

    @Override
    public FeasibilityOutput sendQuery(Location location, AQLinput aqlQuery) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            JSONObject aql = new JSONObject();
            aql.put("aql", aqlQuery.aql());
            HttpEntity request = new HttpEntity(aql.toString(), headers);
            RestTemplate restTemplate = new RestTemplate();
            final String uri = location.localQueryEndpoint();
            ResponseEntity<FeasibilityOutput> result = restTemplate.postForEntity(uri, request, FeasibilityOutput.class);
            return result.getBody();
        } catch (ResourceAccessException e) {
            log.warn("Location " + location.name() + " could not be reached. Error: " + e);
            FeasibilityOutput feasabilityOutput = new FeasibilityOutput();
            feasabilityOutput.setLocation(location.name());
            feasabilityOutput.setPatients("Error");
            return feasabilityOutput;
        }

    }
}
