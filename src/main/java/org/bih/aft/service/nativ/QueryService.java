package org.bih.aft.service.nativ;

import org.bih.aft.controller.dao.AqlWithParams;
import org.bih.aft.service.dao.FeasibilityOutput;
import org.bih.aft.service.dao.Location;

public interface QueryService {
    FeasibilityOutput sendQuery(Location location, AqlWithParams aqlQuery);
}
