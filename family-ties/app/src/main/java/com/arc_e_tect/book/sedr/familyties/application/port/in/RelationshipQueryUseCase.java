package com.arc_e_tect.book.sedr.familyties.application.port.in;

import java.util.List;

import com.arc_e_tect.book.sedr.familyties.application.domain.model.Person;
import com.arc_e_tect.book.sedr.familyties.application.domain.model.RelationshipType;

public interface RelationshipQueryUseCase {
    List<Person> findRelations(String lastName, RelationshipType type);
}
