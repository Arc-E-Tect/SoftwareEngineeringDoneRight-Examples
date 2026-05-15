package com.arc_e_tect.examples.familyties.adapters.outbound.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.arc_e_tect.examples.familyties.application.domain.model.Person;
import com.arc_e_tect.examples.familyties.application.port.outbound.PersonRepositoryPort;

@Component
public class PersonPersistenceAdapter implements PersonRepositoryPort {

    private final PersonJpaRepository repository;

    public PersonPersistenceAdapter(PersonJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Person save(Person person) {
        PersonEntity entity = toEntity(person);
        PersonEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Person> findByFirstAndLastName(String firstName, String lastName) {
        return repository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase(firstName, lastName)
                .map(this::toDomain);
    }

    @Override
    public List<Person> findByLastName(String lastName, int page, int size) {
        return repository.findByLastNamePaged(lastName, page, size)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Person person) {
        repository.findById(person.getId()).ifPresent(repository::delete);
    }

    @Override
    public Optional<Person> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    private PersonEntity toEntity(Person person) {
        PersonEntity entity = new PersonEntity();
        entity.setId(person.getId());
        entity.setFirstName(person.getFirstName());
        entity.setLastName(person.getLastName());
        return entity;
    }

    private Person toDomain(PersonEntity entity) {
        return new Person(entity.getId(), entity.getFirstName(), entity.getLastName());
    }
}
