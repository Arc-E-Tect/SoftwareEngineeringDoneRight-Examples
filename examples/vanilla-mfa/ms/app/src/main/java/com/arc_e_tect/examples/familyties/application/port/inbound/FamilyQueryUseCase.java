package com.arc_e_tect.examples.familyties.application.port.inbound;

import java.util.List;

import com.arc_e_tect.examples.familyties.application.domain.model.Person;

public interface FamilyQueryUseCase {
    List<Person> getFamilyMembers(String lastName, int page, int size);
}
