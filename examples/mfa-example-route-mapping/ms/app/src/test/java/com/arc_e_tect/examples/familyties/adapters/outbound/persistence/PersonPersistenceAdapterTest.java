package com.arc_e_tect.examples.familyties.adapters.outbound.persistence;

import com.arc_e_tect.examples.familyties.application.domain.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PersonPersistenceAdapterTest {

    private PersonJpaRepository repository;
    private PersonPersistenceAdapter adapter;
    private UUID personId;
    private Person person;
    private PersonEntity entity;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(PersonJpaRepository.class);
        adapter = new PersonPersistenceAdapter(repository);
        
        personId = UUID.randomUUID();
        person = new Person(personId, "John", "Doe");
        entity = new PersonEntity();
        entity.setId(personId);
        entity.setFirstName("John");
        entity.setLastName("Doe");
    }

    @Test
    @DisplayName("saves a new person and returns the mapped domain object")
    void saveCreatesNewPerson() {
        when(repository.save(any(PersonEntity.class))).thenReturn(entity);

        Person result = adapter.save(person);

        assertThat(result.getId()).isEqualTo(personId);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        verify(repository).save(any(PersonEntity.class));
    }

    @Test
    @DisplayName("finds a person by first and last name")
    void findByFirstAndLastNameReturnsMatchingPersons() {
        when(repository.findByFirstNameIgnoreCaseAndLastNameIgnoreCase("John", "Doe"))
                .thenReturn(Optional.of(entity));

        Optional<Person> result = adapter.findByFirstAndLastName("John", "Doe");

        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("John");
        assertThat(result.get().getLastName()).isEqualTo("Doe");
    }

    @Test
    @DisplayName("finds people by last name with pagination")
    void findByLastNameReturnsMatchingPersons() {
        // Create a partial mock that calls real default methods
        PersonJpaRepository repoWithDefaults = Mockito.mock(PersonJpaRepository.class, Mockito.CALLS_REAL_METHODS);
        PersonPersistenceAdapter adapterWithDefaults = new PersonPersistenceAdapter(repoWithDefaults);
        
        when(repoWithDefaults.findByLastNameIgnoreCase(eq("Doe"), any())).thenReturn(List.of(entity));

        List<Person> results = adapterWithDefaults.findByLastName("Doe", 0, 10);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getLastName()).isEqualTo("Doe");
    }

    @Test
    @DisplayName("deletes a person when it exists")
    void deleteRemovesPerson() {
        when(repository.findById(personId)).thenReturn(Optional.of(entity));
        
        adapter.delete(person);

        verify(repository).delete(entity);
    }

    @Test
    @DisplayName("finds a person by id when present")
    void findByIdReturnsPersonWhenExists() {
        when(repository.findById(personId)).thenReturn(Optional.of(entity));

        Optional<Person> result = adapter.findById(personId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(personId);
    }

    @Test
    @DisplayName("returns empty when person id does not exist")
    void findByIdReturnsEmptyWhenNotExists() {
        when(repository.findById(personId)).thenReturn(Optional.empty());

        Optional<Person> result = adapter.findById(personId);

        assertThat(result).isEmpty();
    }
}
