package com.arc_e_tect.book.sedr.familyties.application.port.in;

import java.util.List;

import com.arc_e_tect.book.sedr.familyties.application.domain.model.Person;

public interface FamilyQueryUseCase {
    List<Person> getFamilyMembers(String lastName, int page, int size);
}
