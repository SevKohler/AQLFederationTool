package org.bih.aft.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.bih.aft.exceptions.InvalidCountQuery;
import org.bih.aft.controller.dao.AQLinput;
import org.bih.aft.ports.QueryUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/query")
@Slf4j
public class QueryController {

    private final QueryUseCase queryUseCaseService;

    public QueryController( QueryUseCase queryService) {
        this.queryUseCaseService = queryService;
    }
    @PostMapping(
            path = "/federate",
            consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<Object> federateQuery(@RequestBody AQLinput aQlQuery) {
        log.info("Query received");
        try {
            return new ResponseEntity<>(queryUseCaseService.federate(aQlQuery), HttpStatus.OK);
        }catch (InvalidCountQuery invalidCountException){
            return new ResponseEntity<>("{ \"message\" : "+invalidCountException.getMessage()+" }", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(
            path = "/local",
            consumes = "application/json",
            produces = "application/json")
    public ResponseEntity<Object> localQuery(@RequestBody String json) {
        log.info("Query received");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            AQLinput aQlQuery = objectMapper.readValue(json, AQLinput.class);
            return new ResponseEntity<>(queryUseCaseService.local(aQlQuery), HttpStatus.OK);
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>("{ \"message\" : \"Json malformed\" }", HttpStatus.BAD_REQUEST);
        }catch (InvalidCountQuery invalidCountException){
            return new ResponseEntity<>("{ \"message\" : "+invalidCountException.getMessage()+" }", HttpStatus.BAD_REQUEST);
        }
    }


}
