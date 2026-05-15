package com.arc_e_tect.examples.familyties.application.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.arc_e_tect.examples.familyties.application.domain.model.Person;
import com.arc_e_tect.examples.familyties.application.domain.model.RelationshipType;
import com.arc_e_tect.examples.familyties.application.port.inbound.FamilyQueryUseCase;
import com.arc_e_tect.examples.familyties.application.port.inbound.PersonCommandUseCase;
import com.arc_e_tect.examples.familyties.application.port.inbound.RelationshipCommandUseCase;
import com.arc_e_tect.examples.familyties.application.port.inbound.RelationshipQueryUseCase;

@SpringBootTest
@ActiveProfiles("testcomponent")
@Transactional
class FamilyTiesServiceComponentTest {

    @Autowired
    private PersonCommandUseCase personCommandUseCase;

    @Autowired
    private FamilyQueryUseCase familyQueryUseCase;

    @Autowired
    private RelationshipCommandUseCase relationshipCommandUseCase;

    @Autowired
    private RelationshipQueryUseCase relationshipQueryUseCase;

    @Test
    @DisplayName("adds a person and makes them queryable by last name")
    void addsPersonAndFindsByLastName() {
        Person created = personCommandUseCase.addPerson("John", "Smith");

        List<Person> results = familyQueryUseCase.getFamilyMembers("Smith", 0, 10);

        assertThat(created.getId()).isNotNull();
        assertThat(results)
                .extracting(Person::getFirstName, Person::getLastName)
                .contains(tuple("John", "Smith"));
    }

    @Test
    @DisplayName("deletes a person so they no longer appear in family members")
    void deletesPersonAndRemovesFromFamilyMembers() {
        personCommandUseCase.addPerson("Jane", "Doe");

        personCommandUseCase.deletePerson("Jane", "Doe");

        List<Person> results = familyQueryUseCase.getFamilyMembers("Doe", 0, 10);
        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("adds a relationship and returns related people")
    void addsRelationshipAndFindsRelations() {
        personCommandUseCase.addPerson("John", "Smith");
        personCommandUseCase.addPerson("Jane", "Smith");

        relationshipCommandUseCase.addRelationship("John", "Smith", "Jane", "Smith", RelationshipType.PARENT);

        List<Person> relations = relationshipQueryUseCase.findRelations("Smith", RelationshipType.PARENT);

        assertThat(relations)
                .extracting(Person::getFirstName, Person::getLastName)
                .contains(tuple("John", "Smith"), tuple("Jane", "Smith"));
    }
}
