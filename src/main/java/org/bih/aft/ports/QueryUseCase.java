package org.bih.aft.ports;

import org.bih.aft.controller.dao.AQLinput;
import org.bih.aft.service.dao.FeasabilityOutput;

import java.util.List;

/**
 * Input port for querying.
 */
public interface QueryUseCase {
    List<FeasabilityOutput> federate(AQLinput query);

    FeasabilityOutput local(AQLinput query);
}
