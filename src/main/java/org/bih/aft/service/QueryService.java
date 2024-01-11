package org.bih.aft.service;

import org.bih.aft.controller.dao.AQLinput;
import org.bih.aft.service.dao.FeasibilityOutput;
import org.bih.aft.service.dao.Location;

public interface QueryService{
        public FeasibilityOutput sendQuery (Location location, AQLinput aqlQuery);
}
