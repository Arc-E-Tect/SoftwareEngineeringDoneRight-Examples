package com.arc_e_tect.examples.familyties.application.port.outbound;

import java.util.List;
import java.util.UUID;

import com.arc_e_tect.examples.familyties.application.domain.model.Relationship;
import com.arc_e_tect.examples.familyties.application.domain.model.RelationshipType;

public interface RelationshipRepositoryPort {
    Relationship save(Relationship relationship);
    List<Relationship> findByFromPersonId(UUID fromPersonId);
    List<Relationship> findByToPersonId(UUID toPersonId);
    List<Relationship> findByLastNameAndType(String lastName, RelationshipType type);
    long countByToPersonIdAndType(UUID toPersonId, RelationshipType type);
    boolean existsBetween(UUID fromPersonId, UUID toPersonId, RelationshipType type);
    boolean hasRelationshipOfType(UUID personId, RelationshipType type);
}
