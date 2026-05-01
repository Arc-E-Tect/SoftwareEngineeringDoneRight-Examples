package com.arc_e_tect.book.sedr.familyties.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.arc_e_tect.book.sedr.familyties.application.domain.model.Person;

@SpringBootTest
@ActiveProfiles("testcomponent")
@Transactional
class FamilyQueryUseCaseComponentTest {

    @Autowired
    private FamilyQueryUseCase familyQueryUseCase;

    @Autowired
    private PersonCommandUseCase personCommandUseCase;

    @Test
    @DisplayName("returns family members by last name")
    void returnsFamilyMembersByLastName() {
        personCommandUseCase.addPerson("John", "Smith");
        personCommandUseCase.addPerson("Jane", "Smith");

        List<Person> results = familyQueryUseCase.getFamilyMembers("Smith", 0, 10);

        assertThat(results)
                .extracting(Person::getFirstName, Person::getLastName)
                .contains(tuple("John", "Smith"), tuple("Jane", "Smith"));
    }
}
