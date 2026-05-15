package com.arc_e_tect.book.sedr.familyties.application.port.outbound;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.arc_e_tect.book.sedr.familyties.application.domain.model.Person;

public interface PersonRepositoryPort {
    Person save(Person person);
    Optional<Person> findByFirstAndLastName(String firstName, String lastName);
    List<Person> findByLastName(String lastName, int page, int size);
    void delete(Person person);
    Optional<Person> findById(UUID id);
}
