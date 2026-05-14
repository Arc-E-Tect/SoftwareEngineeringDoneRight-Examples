package com.arc_e_tect.examples.familyties.application.port.inbound;

import com.arc_e_tect.examples.familyties.application.domain.model.Relationship;
import com.arc_e_tect.examples.familyties.application.domain.model.RelationshipType;

public interface RelationshipCommandUseCase {
    Relationship addRelationship(String fromFirstName, String fromLastName, String toFirstName, String toLastName, RelationshipType type);
}
