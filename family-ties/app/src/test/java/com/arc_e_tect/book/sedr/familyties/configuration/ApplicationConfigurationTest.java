package com.arc_e_tect.book.sedr.familyties.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.arc_e_tect.book.sedr.familyties.application.domain.service.FamilyTiesService;
import com.arc_e_tect.book.sedr.familyties.application.port.out.PersonRepositoryPort;
import com.arc_e_tect.book.sedr.familyties.application.port.out.RelationshipRepositoryPort;

class ApplicationConfigurationTest {

    @Test
    @DisplayName("creates a FamilyTiesService bean from configuration")
    void createsFamilyTiesServiceBean() {
        ApplicationConfiguration config = new ApplicationConfiguration();
        PersonRepositoryPort personRepo = mock(PersonRepositoryPort.class);
        RelationshipRepositoryPort relationshipRepo = mock(RelationshipRepositoryPort.class);

        FamilyTiesService service = config.familyTiesService(personRepo, relationshipRepo);

        assertThat(service).isNotNull();
    }
}
