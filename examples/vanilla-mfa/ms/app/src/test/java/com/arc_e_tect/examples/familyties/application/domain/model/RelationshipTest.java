package com.arc_e_tect.examples.familyties.application.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RelationshipTest {

        @Test
        @DisplayName("creates a new relationship with generated id")
        void createsNewRelationship() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        Relationship relationship = Relationship.createNew(fromId, toId, RelationshipType.PARENT);

        assertThat(relationship.getFromPersonId()).isEqualTo(fromId);
        assertThat(relationship.getToPersonId()).isEqualTo(toId);
        assertThat(relationship.getType()).isEqualTo(RelationshipType.PARENT);
        assertThat(relationship.getId()).isNotNull();
    }

        @Test
        @DisplayName("requires all constructor arguments to be non-null")
        void requiresAllArguments() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();

        assertThatThrownBy(() -> new Relationship(null, fromId, toId, RelationshipType.PARENT))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Relationship(UUID.randomUUID(), null, toId, RelationshipType.PARENT))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Relationship(UUID.randomUUID(), fromId, null, RelationshipType.PARENT))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Relationship(UUID.randomUUID(), fromId, toId, null))
                .isInstanceOf(NullPointerException.class);
    }
}
