package org.bih.aft.service.nativ;

import org.bih.aft.controller.dao.AqlWithParams;
import org.bih.aft.service.dao.FeasibilityOutput;
import org.bih.aft.service.dao.Location;

import java.util.concurrent.CompletableFuture;

public interface QueryService {
    CompletableFuture<FeasibilityOutput> sendQuery(Location location, AqlWithParams aqlQuery);
}
