package com.arc_e_tect.book.sedr.familyties.adapters.outbound.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.arc_e_tect.book.sedr.familyties.application.domain.model.RelationshipType;

public interface RelationshipJpaRepository extends JpaRepository<RelationshipEntity, UUID> {
    List<RelationshipEntity> findByFromPersonId(UUID fromPersonId);
    List<RelationshipEntity> findByToPersonId(UUID toPersonId);
    long countByToPersonIdAndType(UUID toPersonId, RelationshipType type);
    boolean existsByFromPersonIdAndToPersonIdAndType(UUID fromPersonId, UUID toPersonId, RelationshipType type);
    boolean existsByFromPersonIdAndType(UUID personId, RelationshipType type);
    boolean existsByToPersonIdAndType(UUID personId, RelationshipType type);
}
