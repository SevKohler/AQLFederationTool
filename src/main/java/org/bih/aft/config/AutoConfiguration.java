package org.bih.aft.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class AutoConfiguration implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(AutoConfiguration.class);

    public AutoConfiguration() {
    }

    @Override
    public void afterPropertiesSet() throws IOException {
        /*ConceptSearchService conceptSearchService = new ConceptSearchService(conceptService, conceptRelationshipService);
        DefaultConverterServices defaultConverterServices = new DefaultConverterServices(conceptService, conceptSearchService, new DVTextCodeToConceptConverter(conceptSearchService), persistenceService);
        loadPDMappings(defaultConverterServices);
        loadMDMappings(defaultConverterServices);*/
    }
}