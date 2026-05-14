package com.arc_e_tect.book.sedr.familyties.application.port.inbound;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.arc_e_tect.book.sedr.familyties.application.domain.model.Relationship;
import com.arc_e_tect.book.sedr.familyties.application.domain.model.RelationshipType;

@SpringBootTest
@ActiveProfiles("testcomponent")
@Transactional
class RelationshipCommandUseCaseComponentTest {

    @Autowired
    private RelationshipCommandUseCase relationshipCommandUseCase;

    @Autowired
    private PersonCommandUseCase personCommandUseCase;

    @Test
    @DisplayName("adds a relationship through the command use-case")
    void addsRelationshipThroughUseCase() {
        personCommandUseCase.addPerson("John", "Smith");
        personCommandUseCase.addPerson("Jane", "Smith");

        Relationship created = relationshipCommandUseCase.addRelationship("John", "Smith", "Jane", "Smith", RelationshipType.PARENT);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getType()).isEqualTo(RelationshipType.PARENT);
    }
}
