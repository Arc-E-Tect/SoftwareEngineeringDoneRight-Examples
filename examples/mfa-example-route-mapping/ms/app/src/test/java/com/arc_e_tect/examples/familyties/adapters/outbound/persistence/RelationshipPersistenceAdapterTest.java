package com.arc_e_tect.examples.familyties.adapters.outbound.persistence;

import com.arc_e_tect.examples.familyties.application.port.outbound.PersonRepositoryPort;
import com.arc_e_tect.examples.familyties.application.domain.model.Person;
import com.arc_e_tect.examples.familyties.application.domain.model.Relationship;
import com.arc_e_tect.examples.familyties.application.domain.model.RelationshipType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RelationshipPersistenceAdapterTest {

    private RelationshipJpaRepository repository;
    private PersonJpaRepository personJpaRepository;
    private PersonRepositoryPort personPort;
    private RelationshipPersistenceAdapter adapter;

    private UUID fromPersonId;
    private UUID toPersonId;
    private Relationship relationship;
    private RelationshipEntity entity;
    private PersonEntity fromEntity;
    private PersonEntity toEntity;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(RelationshipJpaRepository.class);
        personJpaRepository = Mockito.mock(PersonJpaRepository.class);
        personPort = Mockito.mock(PersonRepositoryPort.class);
        adapter = new RelationshipPersistenceAdapter(repository, personJpaRepository, personPort);
        
        fromPersonId = UUID.randomUUID();
        toPersonId = UUID.randomUUID();
        relationship = new Relationship(UUID.randomUUID(), fromPersonId, toPersonId, RelationshipType.PARENT);

        fromEntity = new PersonEntity();
        fromEntity.setId(fromPersonId);
        fromEntity.setFirstName("John");
        fromEntity.setLastName("Doe");

        toEntity = new PersonEntity();
        toEntity.setId(toPersonId);
        toEntity.setFirstName("Jane");
        toEntity.setLastName("Doe");

        entity = new RelationshipEntity();
        entity.setId(relationship.getId());
        entity.setFromPerson(fromEntity);
        entity.setToPerson(toEntity);
        entity.setType(RelationshipType.PARENT);
    }

    @Test
    @DisplayName("saves a new relationship and maps ids correctly")
    void saveCreatesNewRelationship() {
        when(personJpaRepository.getReferenceById(fromPersonId)).thenReturn(fromEntity);
        when(personJpaRepository.getReferenceById(toPersonId)).thenReturn(toEntity);
        when(repository.save(any(RelationshipEntity.class))).thenReturn(entity);

        Relationship result = adapter.save(relationship);

        assertThat(result.getId()).isEqualTo(relationship.getId());
        assertThat(result.getFromPersonId()).isEqualTo(fromPersonId);
        assertThat(result.getToPersonId()).isEqualTo(toPersonId);
        verify(repository).save(any(RelationshipEntity.class));
    }

    @Test
    @DisplayName("finds relationships by from-person id")
    void findByFromPersonIdReturnsRelationships() {
        when(repository.findByFromPersonId(fromPersonId)).thenReturn(List.of(entity));

        List<Relationship> results = adapter.findByFromPersonId(fromPersonId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFromPersonId()).isEqualTo(fromPersonId);
    }

    @Test
    @DisplayName("finds relationships by to-person id")
    void findByToPersonIdReturnsRelationships() {
        when(repository.findByToPersonId(toPersonId)).thenReturn(List.of(entity));

        List<Relationship> results = adapter.findByToPersonId(toPersonId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getToPersonId()).isEqualTo(toPersonId);
    }

    @Test
    @DisplayName("filters relationships by last name and type")
    void findByLastNameAndTypeFiltersCorrectly() {
        when(personPort.findByLastName("Doe", 0, 100))
                .thenReturn(List.of(new Person(fromPersonId, "John", "Doe")));
        when(repository.findByFromPersonId(fromPersonId)).thenReturn(List.of(entity));

        List<Relationship> results = adapter.findByLastNameAndType("Doe", RelationshipType.PARENT);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getType()).isEqualTo(RelationshipType.PARENT);
    }

    @Test
    @DisplayName("excludes relationships with a different type")
    void findByLastNameAndTypeFiltersOutNonMatchingTypes() {
        entity.setType(RelationshipType.SPOUSE);
        when(personPort.findByLastName("Doe", 0, 100))
                .thenReturn(List.of(new Person(fromPersonId, "John", "Doe")));
        when(repository.findByFromPersonId(fromPersonId)).thenReturn(List.of(entity));

        List<Relationship> results = adapter.findByLastNameAndType("Doe", RelationshipType.PARENT);

        assertThat(results).isEmpty();
    }

    @Test
    @DisplayName("counts relationships by target person and type")
    void countByToPersonIdAndTypeReturnsCount() {
        when(repository.countByToPersonIdAndType(toPersonId, RelationshipType.PARENT)).thenReturn(2L);

        long count = adapter.countByToPersonIdAndType(toPersonId, RelationshipType.PARENT);

        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("returns true when a relationship exists between people")
    void existsBetweenReturnsTrueWhenExists() {
        when(repository.existsByFromPersonIdAndToPersonIdAndType(fromPersonId, toPersonId, RelationshipType.PARENT))
                .thenReturn(true);

        boolean exists = adapter.existsBetween(fromPersonId, toPersonId, RelationshipType.PARENT);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("returns false when a relationship does not exist")
    void existsBetweenReturnsFalseWhenNotExists() {
        when(repository.existsByFromPersonIdAndToPersonIdAndType(fromPersonId, toPersonId, RelationshipType.PARENT))
                .thenReturn(false);

        boolean exists = adapter.existsBetween(fromPersonId, toPersonId, RelationshipType.PARENT);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("detects relationships of a type for from-person")
    void hasRelationshipOfTypeReturnsTrueForFromPerson() {
        when(repository.existsByFromPersonIdAndType(fromPersonId, RelationshipType.SPOUSE)).thenReturn(true);

        boolean has = adapter.hasRelationshipOfType(fromPersonId, RelationshipType.SPOUSE);

        assertThat(has).isTrue();
    }

    @Test
    @DisplayName("detects relationships of a type for to-person")
    void hasRelationshipOfTypeReturnsTrueForToPerson() {
        when(repository.existsByFromPersonIdAndType(toPersonId, RelationshipType.SPOUSE)).thenReturn(false);
        when(repository.existsByToPersonIdAndType(toPersonId, RelationshipType.SPOUSE)).thenReturn(true);

        boolean has = adapter.hasRelationshipOfType(toPersonId, RelationshipType.SPOUSE);

        assertThat(has).isTrue();
    }

    @Test
    @DisplayName("returns false when no relationships of the type exist")
    void hasRelationshipOfTypeReturnsFalseWhenNone() {
        when(repository.existsByFromPersonIdAndType(fromPersonId, RelationshipType.SPOUSE)).thenReturn(false);
        when(repository.existsByToPersonIdAndType(fromPersonId, RelationshipType.SPOUSE)).thenReturn(false);

        boolean has = adapter.hasRelationshipOfType(fromPersonId, RelationshipType.SPOUSE);

        assertThat(has).isFalse();
    }
}
