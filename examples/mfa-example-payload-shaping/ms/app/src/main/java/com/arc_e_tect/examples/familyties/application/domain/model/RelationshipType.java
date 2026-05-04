package com.arc_e_tect.examples.familyties.application.domain.model;

public enum RelationshipType {
    PARENT,
    GRANDPARENT,
    SIBLING,
    CHILD,
    SPOUSE,
    COUSIN,
    UNCLE,
    AUNT,
    NEPHEW,
    NIECE;

    public static RelationshipType fromName(String name) {
        return valueOf(name.toUpperCase());
    }
}
