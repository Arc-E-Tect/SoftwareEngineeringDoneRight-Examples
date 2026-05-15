package com.arc_e_tect.examples.familyties.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.arc_e_tect.examples.familyties.application.domain.service.FamilyTiesService;
import com.arc_e_tect.examples.familyties.application.port.outbound.PersonRepositoryPort;
import com.arc_e_tect.examples.familyties.application.port.outbound.RelationshipRepositoryPort;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public FamilyTiesService familyTiesService(PersonRepositoryPort personRepositoryPort, RelationshipRepositoryPort relationshipRepositoryPort) {
        return new FamilyTiesService(personRepositoryPort, relationshipRepositoryPort);
    }
}
