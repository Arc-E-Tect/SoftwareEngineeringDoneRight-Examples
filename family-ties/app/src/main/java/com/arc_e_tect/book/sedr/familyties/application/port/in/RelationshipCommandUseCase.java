package com.arc_e_tect.book.sedr.familyties.application.port.in;

import com.arc_e_tect.book.sedr.familyties.application.domain.model.Relationship;
import com.arc_e_tect.book.sedr.familyties.application.domain.model.RelationshipType;

public interface RelationshipCommandUseCase {
    Relationship addRelationship(String fromFirstName, String fromLastName, String toFirstName, String toLastName, RelationshipType type);
}
