package org.bih.aft.ports;

import org.bih.aft.controller.dao.AqlWithParams;
import org.bih.aft.service.dao.FeasibilityOutput;

import java.util.List;

/**
 * Input port for querying.
 */
public interface QueryUseCase {
    List<FeasibilityOutput> federate(AqlWithParams query);

    FeasibilityOutput local(AqlWithParams query);
}
