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
        FederationList expected = new FederationList();
        Location location = new Location();
        location.setName("Test2");
        location.setUrl("https://localhost:8091");
        expected.setLocations(Lists.list(location));

        assertEquals(expected, actual);
    }
}
