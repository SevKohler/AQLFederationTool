package org.bih.aft.service;

import org.assertj.core.util.Lists;
import org.bih.aft.service.dao.Location;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FederationListServiceTest {

    @Test
    void getFederationList() {
        LocationProvider service = new FederationListService();
        List<Location> actual = service.locations();
        Location location = new Location("Test2", "https://localhost:8091");
        List<Location> expected = Lists.list(location);

        assertEquals(expected, actual);
    }
}
