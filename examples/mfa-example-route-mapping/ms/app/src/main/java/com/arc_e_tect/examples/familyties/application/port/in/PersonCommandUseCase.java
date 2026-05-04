package com.arc_e_tect.examples.familyties.application.port.in;

import com.arc_e_tect.examples.familyties.application.domain.model.Person;

public interface PersonCommandUseCase {
    Person addPerson(String firstName, String lastName);
    void deletePerson(String firstName, String lastName);
}
