package org.bih.aft.config;

import lombok.Data;
import org.bih.aft.service.dao.Location;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "aft")
public class AftProperties {

    private Protocol protocol;

    private String location;

    private List<Location> remoteLocations;

    private int minHits = 10;
}

enum Protocol {
    NATIVE,
    BEAM,
}
