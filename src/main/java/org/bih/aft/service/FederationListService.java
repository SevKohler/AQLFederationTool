package org.bih.aft.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bih.aft.config.AftProperties;
import org.bih.aft.service.dao.Location;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class FederationListService implements LocationProvider {

    private final AftProperties aftProperties;

    public List<Location> locations() {
        return aftProperties.getRemoteLocations();
    }
}
