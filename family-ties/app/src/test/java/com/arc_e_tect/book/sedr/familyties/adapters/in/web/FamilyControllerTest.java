package com.arc_e_tect.book.sedr.familyties.adapters.inbound.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

import com.arc_e_tect.book.sedr.familyties.adapters.inbound.web.dto.PersonRequest;
import com.arc_e_tect.book.sedr.familyties.adapters.inbound.web.dto.PersonResponse;
import com.arc_e_tect.book.sedr.familyties.application.domain.model.Person;
import com.arc_e_tect.book.sedr.familyties.application.port.inbound.FamilyQueryUseCase;
import com.arc_e_tect.book.sedr.familyties.application.port.inbound.PersonCommandUseCase;

class FamilyControllerTest {

    private PersonCommandUseCase personCommand;
    private FamilyQueryUseCase familyQuery;
    private FamilyController controller;

    @BeforeEach
    void setUp() {
        personCommand = Mockito.mock(PersonCommandUseCase.class);
        familyQuery = Mockito.mock(FamilyQueryUseCase.class);
        controller = new FamilyController(personCommand, familyQuery);
    }

    @Test
    @DisplayName("adds a person and returns a created response")
    void addsPersonAndReturnsCreated() {
        Person person = new Person(UUID.randomUUID(), "John", "Smith");
        when(personCommand.addPerson("John", "Smith")).thenReturn(person);
        PersonRequest request = new PersonRequest();
        request.setFirstName("John");
        request.setLastName("Smith");

        ResponseEntity<PersonResponse> response = controller.addPerson(request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getHeaders().getLocation()).hasToString("/v1/familyties/Smith");
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFirstName()).isEqualTo("John");
    }

    @Test
    @DisplayName("returns not found when no family members exist")
    void returnsNotFoundWhenNoFamilyMembers() {
        when(familyQuery.getFamilyMembers("Smith", 0, 10)).thenReturn(Collections.emptyList());

        ResponseEntity<List<PersonResponse>> response = controller.findByLastName("Smith", 0, 10);

        assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    @DisplayName("returns family members when present")
    void returnsFamilyMembersWhenPresent() {
        Person john = new Person(UUID.randomUUID(), "John", "Smith");
        when(familyQuery.getFamilyMembers("Smith", 0, 10)).thenReturn(List.of(john));

        ResponseEntity<List<PersonResponse>> response = controller.findByLastName("Smith", 0, 10);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    @DisplayName("deletes a person and returns success")
    void deletesPerson() {
        ResponseEntity<Void> response = controller.delete("Smith", "John");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        verify(personCommand).deletePerson("John", "Smith");
    }
}
