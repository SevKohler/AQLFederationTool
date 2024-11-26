package org.bih.aft.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.net.URL;

@ConditionalOnProperty(value = "aft.protocol", havingValue = "BEAM")
@Data
@Configuration
@ConfigurationProperties(prefix = "beam")
@Validated
public class BeamProperties {

    @NotNull
    private URL proxyUrl;

    @NotBlank
    private String proxyID;

    @NotBlank
    private String appSecret;

    public String getAppId() {
        return "aftv1.%s".formatted(proxyID);
    }
}
