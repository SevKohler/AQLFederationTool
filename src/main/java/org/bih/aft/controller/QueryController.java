package org.bih.aft.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bih.aft.Exceptions.InvalidCountQuery;
import org.bih.aft.controller.dao.AQLinput;
import org.bih.aft.service.QueryService;
import org.ehrbase.openehr.sdk.client.openehrclient.OpenEhrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/query")
public class QueryController {
    private final OpenEhrClient openEhrClient;
    private static final Logger LOG = LoggerFactory.getLogger(QueryService.class);

    public QueryController(OpenEhrClient openEhrClient) {
        this.openEhrClient = openEhrClient;
    }
    @PostMapping(
            path = "/federate",
            consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<Object> federateQuery(@RequestBody String json) {
        LOG.info("Query received");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            AQLinput aQlQuery = objectMapper.readValue(json, AQLinput.class);
            return new ResponseEntity<>(new QueryService(openEhrClient).federateQuery(aQlQuery), HttpStatus.OK);
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>("{ \"message\" : \"Json malformed\" }", HttpStatus.BAD_REQUEST);
        }catch (InvalidCountQuery invalidCountException){
            return new ResponseEntity<>("{ \"message\" : "+invalidCountException.getMessage()+" }", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(
            path = "/local",
            consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<Object> localQuery(@RequestBody String json) {
        LOG.info("Query received");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            AQLinput aQlQuery = objectMapper.readValue(json, AQLinput.class);
            return new ResponseEntity<>(new QueryService(openEhrClient).localQuery(aQlQuery), HttpStatus.OK);
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>("{ \"message\" : \"Json malformed\" }", HttpStatus.BAD_REQUEST);
        }catch (InvalidCountQuery invalidCountException){
            return new ResponseEntity<>("{ \"message\" : "+invalidCountException.getMessage()+" }", HttpStatus.BAD_REQUEST);
        }
    }


}
