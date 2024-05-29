package org.bih.aft.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bih.aft.service.dao.FederationList;
import org.bih.aft.service.dao.Location;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
public class FederationListService implements LocationProvider {

    private final String federationListPath = "locations.json";

    private FederationList federationList;

    private FederationList loadFederationfile() {
        ObjectMapper mapper = new ObjectMapper();
        try(InputStream inputStream = getClass().getClassLoader().getResourceAsStream(federationListPath)){
            federationList = mapper.readValue(inputStream, FederationList.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return federationList;
    }

    public List<Location> locations() {
        return loadFederationfile().locations();
    }
}
