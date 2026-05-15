package com.arc_e_tect.book.sedr.familyties.application.domain.model;

import java.util.Objects;
import java.util.UUID;

public class Relationship {
    private final UUID id;
    private final UUID fromPersonId;
    private final UUID toPersonId;
    private final RelationshipType type;

    public Relationship(UUID id, UUID fromPersonId, UUID toPersonId, RelationshipType type) {
        this.id = Objects.requireNonNull(id, "id");
        this.fromPersonId = Objects.requireNonNull(fromPersonId, "fromPersonId");
        this.toPersonId = Objects.requireNonNull(toPersonId, "toPersonId");
        this.type = Objects.requireNonNull(type, "type");
    }

    public UUID getId() {
        return id;
    }

    public UUID getFromPersonId() {
        return fromPersonId;
    }

    public UUID getToPersonId() {
        return toPersonId;
    }

    public RelationshipType getType() {
        return type;
    }

    public static Relationship createNew(UUID fromPersonId, UUID toPersonId, RelationshipType type) {
        return new Relationship(UUID.randomUUID(), fromPersonId, toPersonId, type);
    }
}
