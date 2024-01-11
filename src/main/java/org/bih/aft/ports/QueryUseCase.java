package org.bih.aft.ports;

import org.bih.aft.controller.dao.AQLinput;
import org.bih.aft.service.dao.FeasibilityOutput;

import java.util.List;

/**
 * Input port for querying.
 */
public interface QueryUseCase {
    List<FeasibilityOutput> federate(AQLinput query);

    FeasibilityOutput local(AQLinput query);
}
