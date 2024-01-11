package org.bih.aft.service;

import org.bih.aft.service.dao.Location;

import java.util.List;

public interface LocationProvider {
    List<Location> locations();
}
