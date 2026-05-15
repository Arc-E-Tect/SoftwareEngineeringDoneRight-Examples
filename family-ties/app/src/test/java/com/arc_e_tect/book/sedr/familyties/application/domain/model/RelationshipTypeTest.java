package com.arc_e_tect.book.sedr.familyties.application.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RelationshipTypeTest {

    @Test
    @DisplayName("parses relationship type names case-insensitively")
    void parsesCaseInsensitiveName() {
        assertThat(RelationshipType.fromName("parent")).isEqualTo(RelationshipType.PARENT);
    }
}
