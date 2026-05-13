package com.arc_e_tect.examples.familyties.application.port.inbound;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.arc_e_tect.examples.familyties.application.domain.model.Person;

@SpringBootTest
@ActiveProfiles("testcomponent")
@Transactional
class PersonCommandUseCaseComponentTest {

    @Autowired
    private PersonCommandUseCase personCommandUseCase;

    @Autowired
    private FamilyQueryUseCase familyQueryUseCase;

    @Test
    @DisplayName("adds a person through the command use-case")
    void addsPersonThroughUseCase() {
        Person created = personCommandUseCase.addPerson("John", "Smith");

        assertThat(created.getId()).isNotNull();
        assertThat(created.getFirstName()).isEqualTo("John");
        assertThat(created.getLastName()).isEqualTo("Smith");
    }

    @Test
    @DisplayName("deletes a person through the command use-case")
    void deletesPersonThroughUseCase() {
        personCommandUseCase.addPerson("Jane", "Doe");

        personCommandUseCase.deletePerson("Jane", "Doe");

        List<Person> results = familyQueryUseCase.getFamilyMembers("Doe", 0, 10);
        assertThat(results).isEmpty();
    }
}
