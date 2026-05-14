package com.arc_e_tect.examples.familyties.adapters.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import com.arc_e_tect.examples.familyties.adapters.in.web.dto.PersonResponse;
import com.arc_e_tect.examples.familyties.adapters.in.web.dto.RelationshipRequest;
import com.arc_e_tect.examples.familyties.application.domain.model.Person;
import com.arc_e_tect.examples.familyties.application.domain.model.Relationship;
import com.arc_e_tect.examples.familyties.application.domain.model.RelationshipType;
import com.arc_e_tect.examples.familyties.application.port.inbound.RelationshipCommandUseCase;
import com.arc_e_tect.examples.familyties.application.port.inbound.RelationshipQueryUseCase;

class RelationshipControllerTest {

    private RelationshipCommandUseCase commandUseCase;
    private RelationshipQueryUseCase queryUseCase;
    private RelationshipController controller;

    @BeforeEach
    void setUp() {
        commandUseCase = Mockito.mock(RelationshipCommandUseCase.class);
        queryUseCase = Mockito.mock(RelationshipQueryUseCase.class);
        controller = new RelationshipController(commandUseCase, queryUseCase);
    }

    @Test
    @DisplayName("adds a relationship and returns success")
    void addsRelationship() {
        Relationship relationship = new Relationship(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), RelationshipType.SPOUSE);
        when(commandUseCase.addRelationship("John", "Smith", "Jane", "Smith", RelationshipType.SPOUSE)).thenReturn(relationship);
        RelationshipRequest request = new RelationshipRequest();
        request.setFromFirstName("John");
        request.setFromLastName("Smith");
        request.setToFirstName("Jane");
        request.setToLastName("Smith");
        request.setType("spouse");

        ResponseEntity<Relationship> response = controller.addRelationship(request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(commandUseCase).addRelationship("John", "Smith", "Jane", "Smith", RelationshipType.SPOUSE);
    }

    @Test
    @DisplayName("returns not found when no relations are present")
    void returnsNotFoundWhenNoRelations() {
        when(queryUseCase.findRelations("Smith", RelationshipType.SIBLING)).thenReturn(Collections.emptyList());

        ResponseEntity<List<PersonResponse>> response = controller.findRelations("sibling", "Smith");

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    @DisplayName("returns relations when present")
    void returnsRelationsWhenPresent() {
        Person relative = new Person(UUID.randomUUID(), "John", "Smith");
        when(queryUseCase.findRelations("Smith", RelationshipType.SIBLING)).thenReturn(List.of(relative));

        ResponseEntity<List<PersonResponse>> response = controller.findRelations("sibling", "Smith");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).hasSize(1);
    }
}
