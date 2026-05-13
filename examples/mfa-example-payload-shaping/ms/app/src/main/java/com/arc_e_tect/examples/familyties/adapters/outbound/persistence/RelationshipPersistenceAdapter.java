package com.arc_e_tect.examples.familyties.adapters.outbound.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.arc_e_tect.examples.familyties.application.domain.model.Person;
import com.arc_e_tect.examples.familyties.application.domain.model.Relationship;
import com.arc_e_tect.examples.familyties.application.domain.model.RelationshipType;
import com.arc_e_tect.examples.familyties.application.port.outbound.PersonRepositoryPort;
import com.arc_e_tect.examples.familyties.application.port.outbound.RelationshipRepositoryPort;

@Component
public class RelationshipPersistenceAdapter implements RelationshipRepositoryPort {

    private final RelationshipJpaRepository repository;
    private final PersonJpaRepository personJpaRepository;
    private final PersonRepositoryPort personPort;

    public RelationshipPersistenceAdapter(RelationshipJpaRepository repository, PersonJpaRepository personJpaRepository, PersonRepositoryPort personPort) {
        this.repository = repository;
        this.personJpaRepository = personJpaRepository;
        this.personPort = personPort;
    }

    @Override
    public Relationship save(Relationship relationship) {
        RelationshipEntity entity = new RelationshipEntity();
        entity.setId(relationship.getId());
        entity.setFromPerson(personJpaRepository.getReferenceById(relationship.getFromPersonId()));
        entity.setToPerson(personJpaRepository.getReferenceById(relationship.getToPersonId()));
        entity.setType(relationship.getType());
        RelationshipEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<Relationship> findByFromPersonId(UUID fromPersonId) {
        return repository.findByFromPersonId(fromPersonId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Relationship> findByToPersonId(UUID toPersonId) {
        return repository.findByToPersonId(toPersonId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Relationship> findByLastNameAndType(String lastName, RelationshipType type) {
        return personPort.findByLastName(lastName, 0, 100).stream()
                .map(Person::getId)
                .map(repository::findByFromPersonId)
                .flatMap(List::stream)
                .filter(entity -> entity.getType() == type)
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long countByToPersonIdAndType(UUID toPersonId, RelationshipType type) {
        return repository.countByToPersonIdAndType(toPersonId, type);
    }

    @Override
    public boolean existsBetween(UUID fromPersonId, UUID toPersonId, RelationshipType type) {
        return repository.existsByFromPersonIdAndToPersonIdAndType(fromPersonId, toPersonId, type);
    }

    @Override
    public boolean hasRelationshipOfType(UUID personId, RelationshipType type) {
        return repository.existsByFromPersonIdAndType(personId, type) || repository.existsByToPersonIdAndType(personId, type);
    }

    private Relationship toDomain(RelationshipEntity entity) {
        return new Relationship(entity.getId(), entity.getFromPerson().getId(), entity.getToPerson().getId(), entity.getType());
    }
}
