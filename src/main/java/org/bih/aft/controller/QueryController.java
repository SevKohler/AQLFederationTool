package org.bih.aft.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bih.aft.controller.dao.AQLinput;
import org.bih.aft.service.QueryService;
import org.ehrbase.client.openehrclient.OpenEhrClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/query")
public class QueryController {
    private final OpenEhrClient openEhrClient;

    public QueryController(OpenEhrClient openEhrClient) {
        this.openEhrClient = openEhrClient;
    }
    @PostMapping(
            produces = "application/json")
    public ResponseEntity<Object> query(@RequestBody String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            AQLinput aQlQuery = objectMapper.readValue(json, AQLinput.class);
            return new ResponseEntity<>(new QueryService(openEhrClient).query(aQlQuery), HttpStatus.OK);
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>("{ \"message\" : \"Json malformed\" }", HttpStatus.BAD_REQUEST);
        }
    }

}
