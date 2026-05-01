package com.arc_e_tect.book.sedr.familyties.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.arc_e_tect.book.sedr.familyties.application.domain.model.Person;
import com.arc_e_tect.book.sedr.familyties.application.domain.model.RelationshipType;

@SpringBootTest
@ActiveProfiles("testcomponent")
@Transactional
class RelationshipQueryUseCaseComponentTest {

    @Autowired
    private RelationshipQueryUseCase relationshipQueryUseCase;

    @Autowired
    private RelationshipCommandUseCase relationshipCommandUseCase;

    @Autowired
    private PersonCommandUseCase personCommandUseCase;

    @Test
    @DisplayName("finds related people through the query use-case")
    void findsRelatedPeopleThroughUseCase() {
        personCommandUseCase.addPerson("John", "Smith");
        personCommandUseCase.addPerson("Jane", "Smith");

        relationshipCommandUseCase.addRelationship("John", "Smith", "Jane", "Smith", RelationshipType.PARENT);

        List<Person> results = relationshipQueryUseCase.findRelations("Smith", RelationshipType.PARENT);

        assertThat(results)
                .extracting(Person::getFirstName, Person::getLastName)
                .contains(tuple("John", "Smith"), tuple("Jane", "Smith"));
    }
}
