package com.arc_e_tect.examples.familyties.application.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PersonTest {

    @Test
    @DisplayName("creates a new Person while trimming surrounding whitespace")
    void createsPersonWithTrimmedNames() {
        Person person = Person.createNew(" John ", " Smith ");

        assertThat(person.getFirstName()).isEqualTo("John");
        assertThat(person.getLastName()).isEqualTo("Smith");
    }

    @Test
    @DisplayName("throws when creating a new Person and the provided first name is blank")
    void failsOnBlankFirstName() {
        assertThatThrownBy(() -> Person.createNew(" ", "Smith"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("firstName");
    }

    @Test
    @DisplayName("throws when creating a new Person and the provided last name is blank")
    void failsOnBlankLastName() {
        assertThatThrownBy(() -> Person.createNew("John", " "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lastName");
    }

    @Test
    @DisplayName("throws when creating a new Person and the provided first name is null")
    void failsOnNullFirstName() {
        assertThatThrownBy(() -> Person.createNew(null, "Smith"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("firstName");
    }

    @Test
    @DisplayName("throws when creating a new Person and the provided last name is null")
    void failsOnNullLastName() {
        assertThatThrownBy(() -> Person.createNew("John", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lastName");
    }

    @Test
    @DisplayName("withId returns a new instance carrying over names and using the provided id")
    void withIdCreatesNewInstance() {
        Person original = Person.createNew("John", "Smith");
        UUID newId = UUID.randomUUID();

        Person updated = original.withId(newId);

        assertThat(updated.getId()).isEqualTo(newId);
        assertThat(updated.getFirstName()).isEqualTo(original.getFirstName());
        assertThat(updated.getLastName()).isEqualTo(original.getLastName());
        assertThat(updated).isNotSameAs(original);
    }

    @Test
    @DisplayName("sameIdentity compares names case-insensitively")
    void comparesIdentityIgnoringCase() {
        Person a = new Person(UUID.randomUUID(), "John", "Smith");
        Person b = new Person(UUID.randomUUID(), "john", "SMITH");

        assertThat(a.sameIdentity(b)).isTrue();
    }

    @Test
    @DisplayName("sameIdentity returns false when other person is null")
    void comparesIdentityWithNullOther() {
        Person person = Person.createNew("John", "Smith");

        assertThat(person.sameIdentity(null)).isFalse();
    }

    @Test
    @DisplayName("sameIdentity returns false when names differ")
    void comparesIdentityWhenNamesDiffer() {
        Person a = new Person(UUID.randomUUID(), "John", "Smith");
        Person b = new Person(UUID.randomUUID(), "Jane", "Smith");

        assertThat(a.sameIdentity(b)).isFalse();
    }

    @Test
    @DisplayName("sameIdentity returns false when first names match but last names differ")
    void comparesIdentityWhenLastNamesDiffer() {
        Person a = new Person(UUID.randomUUID(), "John", "Smith");
        Person b = new Person(UUID.randomUUID(), "John", "Doe");

        assertThat(a.sameIdentity(b)).isFalse();
    }
}
