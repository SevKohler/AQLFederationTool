package org.bih.aft.service;

import org.assertj.core.util.Lists;
import org.bih.aft.service.dao.FederationList;
import org.bih.aft.service.dao.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FederationListServiceTest {

    @Test
    void getFederationList() {
        FederationListService service = new FederationListService();
        FederationList actual = service.getFederationList();
        Location location = new Location("Test2", "https://localhost:8091");
        FederationList expected = new FederationList(Lists.list(location));

        assertEquals(expected, actual);
    }
}
