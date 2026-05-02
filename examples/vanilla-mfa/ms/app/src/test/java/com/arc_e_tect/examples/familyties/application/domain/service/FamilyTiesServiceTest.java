package com.arc_e_tect.examples.familyties.application.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.arc_e_tect.examples.familyties.application.common.ConflictException;
import com.arc_e_tect.examples.familyties.application.common.NotFoundException;
import com.arc_e_tect.examples.familyties.application.domain.model.Person;
import com.arc_e_tect.examples.familyties.application.domain.model.Relationship;
import com.arc_e_tect.examples.familyties.application.domain.model.RelationshipType;
import com.arc_e_tect.examples.familyties.application.port.out.PersonRepositoryPort;
import com.arc_e_tect.examples.familyties.application.port.out.RelationshipRepositoryPort;

class FamilyTiesServiceTest {

    private PersonRepositoryPort personRepository;
    private RelationshipRepositoryPort relationshipRepository;
    private FamilyTiesService service;

    @BeforeEach
    void setUp() {
        personRepository = Mockito.mock(PersonRepositoryPort.class);
        relationshipRepository = Mockito.mock(RelationshipRepositoryPort.class);
        service = new FamilyTiesService(personRepository, relationshipRepository);
    }

    @Test
    @DisplayName("adds a person when no existing match is found")
    void addsPersonWhenNotExisting() {
        when(personRepository.findByFirstAndLastName("John", "Smith")).thenReturn(Optional.empty());
        when(personRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Person result = service.addPerson("John", "Smith");

        assertThat(result.getFirstName()).isEqualTo("John");
        verify(personRepository).save(any());
    }

    @Test
    @DisplayName("rejects adding a duplicate person")
    void preventsDuplicatePerson() {
        when(personRepository.findByFirstAndLastName("John", "Smith")).thenReturn(Optional.of(Person.createNew("John", "Smith")));

        assertThatThrownBy(() -> service.addPerson("John", "Smith"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("caps page size to the maximum when requesting family members")
    void returnsFamilyMembersWithPaginationBounded() {
        when(personRepository.findByLastName("Smith", 0, 10)).thenReturn(Collections.emptyList());
        service.getFamilyMembers("Smith", 0, 20);
        verify(personRepository).findByLastName("Smith", 0, 10);
    }

    @Test
    @DisplayName("normalizes negative page numbers to zero")
    void normalizesNegativePageToZero() {
        when(personRepository.findByLastName("Smith", 0, 1)).thenReturn(Collections.emptyList());

        service.getFamilyMembers("Smith", -1, 1);

        verify(personRepository).findByLastName("Smith", 0, 1);
    }

    @Test
    @DisplayName("normalizes page size less than one to one")
    void normalizesSizeLessThanOneToOne() {
        when(personRepository.findByLastName("Smith", 0, 1)).thenReturn(Collections.emptyList());

        service.getFamilyMembers("Smith", 0, 0);

        verify(personRepository).findByLastName("Smith", 0, 1);
    }

    @Test
    @DisplayName("adds a parent relationship when constraints are satisfied")
    void addsRelationshipWithConstraints() {
        Person parent = new Person(UUID.randomUUID(), "John", "Smith");
        Person child = new Person(UUID.randomUUID(), "Jane", "Smith");
        when(personRepository.findByFirstAndLastName("John", "Smith")).thenReturn(Optional.of(parent));
        when(personRepository.findByFirstAndLastName("Jane", "Smith")).thenReturn(Optional.of(child));
        when(relationshipRepository.existsBetween(parent.getId(), child.getId(), RelationshipType.PARENT)).thenReturn(false);
        when(relationshipRepository.countByToPersonIdAndType(child.getId(), RelationshipType.PARENT)).thenReturn(0L);
        when(relationshipRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Relationship created = service.addRelationship("John", "Smith", "Jane", "Smith", RelationshipType.PARENT);

        assertThat(created.getType()).isEqualTo(RelationshipType.PARENT);
    }

    @Test
    @DisplayName("rejects duplicate relationships between the same people")
    void preventsDuplicateRelationship() {
        Person from = new Person(UUID.randomUUID(), "John", "Smith");
        Person to = new Person(UUID.randomUUID(), "Jane", "Smith");
        when(personRepository.findByFirstAndLastName("John", "Smith")).thenReturn(Optional.of(from));
        when(personRepository.findByFirstAndLastName("Jane", "Smith")).thenReturn(Optional.of(to));
        when(relationshipRepository.existsBetween(from.getId(), to.getId(), RelationshipType.SPOUSE)).thenReturn(true);

        assertThatThrownBy(() -> service.addRelationship("John", "Smith", "Jane", "Smith", RelationshipType.SPOUSE))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("enforces the maximum number of parents")
    void enforcesParentLimit() {
        Person from = new Person(UUID.randomUUID(), "John", "Smith");
        Person to = new Person(UUID.randomUUID(), "Jane", "Smith");
        when(personRepository.findByFirstAndLastName("John", "Smith")).thenReturn(Optional.of(from));
        when(personRepository.findByFirstAndLastName("Jane", "Smith")).thenReturn(Optional.of(to));
        when(relationshipRepository.existsBetween(from.getId(), to.getId(), RelationshipType.PARENT)).thenReturn(false);
        when(relationshipRepository.countByToPersonIdAndType(to.getId(), RelationshipType.PARENT)).thenReturn(2L);

        assertThatThrownBy(() -> service.addRelationship("John", "Smith", "Jane", "Smith", RelationshipType.PARENT))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("enforces the maximum number of grandparents")
    void enforcesGrandparentLimit() {
        Person from = new Person(UUID.randomUUID(), "John", "Smith");
        Person to = new Person(UUID.randomUUID(), "Jane", "Smith");
        when(personRepository.findByFirstAndLastName("John", "Smith")).thenReturn(Optional.of(from));
        when(personRepository.findByFirstAndLastName("Jane", "Smith")).thenReturn(Optional.of(to));
        when(relationshipRepository.existsBetween(from.getId(), to.getId(), RelationshipType.GRANDPARENT)).thenReturn(false);
        when(relationshipRepository.countByToPersonIdAndType(to.getId(), RelationshipType.GRANDPARENT)).thenReturn(4L);

        assertThatThrownBy(() -> service.addRelationship("John", "Smith", "Jane", "Smith", RelationshipType.GRANDPARENT))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("enforces spouse limit for the source person")
    void enforcesSpouseLimitForEitherParty() {
        Person from = new Person(UUID.randomUUID(), "John", "Smith");
        Person to = new Person(UUID.randomUUID(), "Jane", "Smith");
        when(personRepository.findByFirstAndLastName("John", "Smith")).thenReturn(Optional.of(from));
        when(personRepository.findByFirstAndLastName("Jane", "Smith")).thenReturn(Optional.of(to));
        when(relationshipRepository.existsBetween(from.getId(), to.getId(), RelationshipType.SPOUSE)).thenReturn(false);
        when(relationshipRepository.hasRelationshipOfType(from.getId(), RelationshipType.SPOUSE)).thenReturn(true);

        assertThatThrownBy(() -> service.addRelationship("John", "Smith", "Jane", "Smith", RelationshipType.SPOUSE))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("enforces spouse limit for the target person")
    void enforcesSpouseLimitForTargetPerson() {
        Person from = new Person(UUID.randomUUID(), "John", "Smith");
        Person to = new Person(UUID.randomUUID(), "Jane", "Smith");
        when(personRepository.findByFirstAndLastName("John", "Smith")).thenReturn(Optional.of(from));
        when(personRepository.findByFirstAndLastName("Jane", "Smith")).thenReturn(Optional.of(to));
        when(relationshipRepository.existsBetween(from.getId(), to.getId(), RelationshipType.SPOUSE)).thenReturn(false);
        when(relationshipRepository.hasRelationshipOfType(from.getId(), RelationshipType.SPOUSE)).thenReturn(false);
        when(relationshipRepository.hasRelationshipOfType(to.getId(), RelationshipType.SPOUSE)).thenReturn(true);

        assertThatThrownBy(() -> service.addRelationship("John", "Smith", "Jane", "Smith", RelationshipType.SPOUSE))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    @DisplayName("adds a sibling relationship without additional constraints")
    void addsRelationshipWithoutConstraints() {
        Person from = new Person(UUID.randomUUID(), "John", "Smith");
        Person to = new Person(UUID.randomUUID(), "Jane", "Smith");
        when(personRepository.findByFirstAndLastName("John", "Smith")).thenReturn(Optional.of(from));
        when(personRepository.findByFirstAndLastName("Jane", "Smith")).thenReturn(Optional.of(to));
        when(relationshipRepository.existsBetween(from.getId(), to.getId(), RelationshipType.SIBLING)).thenReturn(false);
        when(relationshipRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Relationship created = service.addRelationship("John", "Smith", "Jane", "Smith", RelationshipType.SIBLING);

        assertThat(created.getType()).isEqualTo(RelationshipType.SIBLING);
    }

    @Test
    @DisplayName("finds relationships in both directions")
    void findsRelationsInBothDirections() {
        Person alice = new Person(UUID.randomUUID(), "Alice", "Smith");
        Person bob = new Person(UUID.randomUUID(), "Bob", "Smith");
        Person carol = new Person(UUID.randomUUID(), "Carol", "Smith");

        when(personRepository.findByLastName("Smith", 0, 100)).thenReturn(List.of(alice));
        when(relationshipRepository.findByFromPersonId(alice.getId())).thenReturn(List.of(new Relationship(UUID.randomUUID(), alice.getId(), bob.getId(), RelationshipType.SIBLING)));
        when(relationshipRepository.findByToPersonId(alice.getId())).thenReturn(List.of(new Relationship(UUID.randomUUID(), carol.getId(), alice.getId(), RelationshipType.SIBLING)));
        when(personRepository.findById(bob.getId())).thenReturn(Optional.of(bob));
        when(personRepository.findById(carol.getId())).thenReturn(Optional.of(carol));

        List<Person> result = service.findRelations("Smith", RelationshipType.SIBLING);

        assertThat(result).containsExactlyInAnyOrder(bob, carol);
    }

    @Test
    @DisplayName("throws when no subject exists for relation lookup")
    void throwsWhenRelationsSubjectMissing() {
        when(personRepository.findByLastName("Ghost", 0, 100)).thenReturn(List.of());

        assertThatThrownBy(() -> service.findRelations("Ghost", RelationshipType.PARENT))
                .isInstanceOf(NotFoundException.class);
        verifyNoInteractions(relationshipRepository);
    }

    @Test
    @DisplayName("deletes an existing person")
    void deletePersonDeletesExisting() {
        Person john = new Person(UUID.randomUUID(), "John", "Smith");
        when(personRepository.findByFirstAndLastName("John", "Smith")).thenReturn(Optional.of(john));

        service.deletePerson("John", "Smith");

        verify(personRepository).delete(john);
    }

    @Test
    @DisplayName("throws when adding relationship and from-person is missing")
    void addRelationshipThrowsWhenPersonMissing() {
        when(personRepository.findByFirstAndLastName("John", "Smith")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addRelationship("John", "Smith", "Jane", "Smith", RelationshipType.SPOUSE))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("throws when adding relationship and to-person is missing")
    void addRelationshipThrowsWhenToPersonMissing() {
        Person john = new Person(UUID.randomUUID(), "John", "Smith");
        when(personRepository.findByFirstAndLastName("John", "Smith")).thenReturn(Optional.of(john));
        when(personRepository.findByFirstAndLastName("Jane", "Smith")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addRelationship("John", "Smith", "Jane", "Smith", RelationshipType.SPOUSE))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("throws when deleting a missing person")
    void throwsWhenPersonMissing() {
        when(personRepository.findByFirstAndLastName("Missing", "Smith")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deletePerson("Missing", "Smith"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("adds a grandparent relationship successfully")
    void addsGrandparentRelationshipSuccessfully() {
        Person grandparent = new Person(UUID.randomUUID(), "John", "Smith");
        Person grandchild = new Person(UUID.randomUUID(), "Jane", "Smith");
        when(personRepository.findByFirstAndLastName("John", "Smith")).thenReturn(Optional.of(grandparent));
        when(personRepository.findByFirstAndLastName("Jane", "Smith")).thenReturn(Optional.of(grandchild));
        when(relationshipRepository.existsBetween(grandparent.getId(), grandchild.getId(), RelationshipType.GRANDPARENT)).thenReturn(false);
        when(relationshipRepository.countByToPersonIdAndType(grandchild.getId(), RelationshipType.GRANDPARENT)).thenReturn(0L);
        when(relationshipRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Relationship created = service.addRelationship("John", "Smith", "Jane", "Smith", RelationshipType.GRANDPARENT);

        assertThat(created.getType()).isEqualTo(RelationshipType.GRANDPARENT);
    }

    @Test
    @DisplayName("adds a spouse relationship successfully")
    void addsSpouseRelationshipSuccessfully() {
        Person husband = new Person(UUID.randomUUID(), "John", "Smith");
        Person wife = new Person(UUID.randomUUID(), "Jane", "Doe");
        when(personRepository.findByFirstAndLastName("John", "Smith")).thenReturn(Optional.of(husband));
        when(personRepository.findByFirstAndLastName("Jane", "Doe")).thenReturn(Optional.of(wife));
        when(relationshipRepository.existsBetween(husband.getId(), wife.getId(), RelationshipType.SPOUSE)).thenReturn(false);
        when(relationshipRepository.hasRelationshipOfType(husband.getId(), RelationshipType.SPOUSE)).thenReturn(false);
        when(relationshipRepository.hasRelationshipOfType(wife.getId(), RelationshipType.SPOUSE)).thenReturn(false);
        when(relationshipRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Relationship created = service.addRelationship("John", "Smith", "Jane", "Doe", RelationshipType.SPOUSE);

        assertThat(created.getType()).isEqualTo(RelationshipType.SPOUSE);
    }

    @Test
    @DisplayName("findRelations ignores relationships with a different type")
    void findRelationsFiltersDifferentTypes() {
        Person alice = new Person(UUID.randomUUID(), "Alice", "Smith");
        Person bob = new Person(UUID.randomUUID(), "Bob", "Smith");
        Person carol = new Person(UUID.randomUUID(), "Carol", "Smith");

        when(personRepository.findByLastName("Smith", 0, 100)).thenReturn(List.of(alice));
        when(relationshipRepository.findByFromPersonId(alice.getId()))
                .thenReturn(List.of(new Relationship(UUID.randomUUID(), alice.getId(), bob.getId(), RelationshipType.PARENT)));
        when(relationshipRepository.findByToPersonId(alice.getId()))
                .thenReturn(List.of(new Relationship(UUID.randomUUID(), carol.getId(), alice.getId(), RelationshipType.PARENT)));
        when(personRepository.findById(bob.getId())).thenReturn(Optional.of(bob));
        when(personRepository.findById(carol.getId())).thenReturn(Optional.of(carol));

        List<Person> result = service.findRelations("Smith", RelationshipType.SIBLING);

        assertThat(result).isEmpty();
    }
}
