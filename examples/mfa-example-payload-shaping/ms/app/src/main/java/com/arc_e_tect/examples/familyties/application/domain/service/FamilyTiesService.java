package com.arc_e_tect.examples.familyties.application.domain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.arc_e_tect.examples.familyties.application.common.ConflictException;
import com.arc_e_tect.examples.familyties.application.common.NotFoundException;
import com.arc_e_tect.examples.familyties.application.domain.model.Person;
import com.arc_e_tect.examples.familyties.application.domain.model.Relationship;
import com.arc_e_tect.examples.familyties.application.domain.model.RelationshipType;
import com.arc_e_tect.examples.familyties.application.port.inbound.FamilyQueryUseCase;
import com.arc_e_tect.examples.familyties.application.port.inbound.PersonCommandUseCase;
import com.arc_e_tect.examples.familyties.application.port.inbound.RelationshipCommandUseCase;
import com.arc_e_tect.examples.familyties.application.port.inbound.RelationshipQueryUseCase;
import com.arc_e_tect.examples.familyties.application.port.outbound.PersonRepositoryPort;
import com.arc_e_tect.examples.familyties.application.port.outbound.RelationshipRepositoryPort;

public class FamilyTiesService implements PersonCommandUseCase, FamilyQueryUseCase, RelationshipCommandUseCase, RelationshipQueryUseCase {

    private final PersonRepositoryPort personRepository;
    private final RelationshipRepositoryPort relationshipRepository;

    public FamilyTiesService(PersonRepositoryPort personRepository, RelationshipRepositoryPort relationshipRepository) {
        this.personRepository = Objects.requireNonNull(personRepository, "personRepository");
        this.relationshipRepository = Objects.requireNonNull(relationshipRepository, "relationshipRepository");
    }

    @Override
    public Person addPerson(String firstName, String lastName) {
        personRepository.findByFirstAndLastName(firstName, lastName)
                .ifPresent(existing -> { throw new ConflictException("Person already exists: " + firstName + " " + lastName); });
        Person person = Person.createNew(firstName, lastName);
        return personRepository.save(person);
    }

    @Override
    public void deletePerson(String firstName, String lastName) {
        Person person = personRepository.findByFirstAndLastName(firstName, lastName)
                .orElseThrow(() -> new NotFoundException("Person not found: " + firstName + " " + lastName));
        personRepository.delete(person);
    }

    @Override
    public List<Person> getFamilyMembers(String lastName, int page, int size) {
        int boundedSize = Math.min(Math.max(size, 1), 10);
        int normalizedPage = Math.max(page, 0);
        return personRepository.findByLastName(lastName, normalizedPage, boundedSize);
    }

    @Override
    public Relationship addRelationship(String fromFirstName, String fromLastName, String toFirstName, String toLastName, RelationshipType type) {
        Person from = getPersonByName(fromFirstName, fromLastName);
        Person to = getPersonByName(toFirstName, toLastName);

        if (relationshipRepository.existsBetween(from.getId(), to.getId(), type)) {
            throw new ConflictException("Relationship already exists");
        }

        enforceConstraints(type, to.getId(), from.getId());

        Relationship relationship = Relationship.createNew(from.getId(), to.getId(), type);
        return relationshipRepository.save(relationship);
    }

    @Override
    public List<Person> findRelations(String lastName, RelationshipType type) {
        List<Person> subjects = personRepository.findByLastName(lastName, 0, 100);
        if (subjects.isEmpty()) {
            throw new NotFoundException("No persons with last name: " + lastName);
        }

        List<Person> related = new ArrayList<>();
        for (Person subject : subjects) {
            List<Relationship> outgoing = relationshipRepository.findByFromPersonId(subject.getId());
            outgoing.stream()
                    .filter(r -> r.getType() == type)
                    .map(r -> personRepository.findById(r.getToPersonId()))
                    .flatMap(Optional::stream)
                    .forEach(related::add);

            List<Relationship> incoming = relationshipRepository.findByToPersonId(subject.getId());
            incoming.stream()
                    .filter(r -> r.getType() == type)
                    .map(r -> personRepository.findById(r.getFromPersonId()))
                    .flatMap(Optional::stream)
                    .forEach(related::add);
        }

        return related.stream()
                .distinct()
                .collect(Collectors.toList());
    }

    private Person getPersonByName(String firstName, String lastName) {
        return personRepository.findByFirstAndLastName(firstName, lastName)
                .orElseThrow(() -> new NotFoundException("Person not found: " + firstName + " " + lastName));
    }

    private void enforceConstraints(RelationshipType type, UUID targetPersonId, UUID sourcePersonId) {
        switch (type) {
            case PARENT -> ensureLimit(targetPersonId, RelationshipType.PARENT, 2, "A person can have at most two parents");
            case GRANDPARENT -> ensureLimit(targetPersonId, RelationshipType.GRANDPARENT, 4, "A person can have at most four grandparents");
            case SPOUSE -> ensureSpouseLimit(targetPersonId, sourcePersonId);
            // No default case needed - all RelationshipType enum values are handled
        }
    }

    private void ensureLimit(UUID personId, RelationshipType type, int max, String message) {
        long current = relationshipRepository.countByToPersonIdAndType(personId, type);
        if (current >= max) {
            throw new ConflictException(message);
        }
    }

    private void ensureSpouseLimit(UUID targetPersonId, UUID sourcePersonId) {
        if (relationshipRepository.hasRelationshipOfType(targetPersonId, RelationshipType.SPOUSE)
                || relationshipRepository.hasRelationshipOfType(sourcePersonId, RelationshipType.SPOUSE)) {
            throw new ConflictException("A person can only have one spouse");
        }
    }
}
