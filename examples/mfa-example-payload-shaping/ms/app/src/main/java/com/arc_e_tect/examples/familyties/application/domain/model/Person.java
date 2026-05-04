package com.arc_e_tect.examples.familyties.application.domain.model;

import java.util.Objects;
import java.util.UUID;

public class Person {
    private final UUID id;
    private final String firstName;
    private final String lastName;

    public Person(UUID id, String firstName, String lastName) {
        this.id = Objects.requireNonNull(id, "id");
        this.firstName = requireNonBlank(firstName, "firstName");
        this.lastName = requireNonBlank(lastName, "lastName");
    }

    public UUID getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public static Person createNew(String firstName, String lastName) {
        return new Person(UUID.randomUUID(), firstName, lastName);
    }

    public Person withId(UUID newId) {
        return new Person(newId, firstName, lastName);
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value.trim();
    }

    public boolean sameIdentity(Person other) {
        return other != null && firstName.equalsIgnoreCase(other.firstName) && lastName.equalsIgnoreCase(other.lastName);
    }
}
