package com.arc_e_tect.book.sedr.familyties.adapters.outbound.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonJpaRepository extends JpaRepository<PersonEntity, UUID> {
    Optional<PersonEntity> findByFirstNameIgnoreCaseAndLastNameIgnoreCase(String firstName, String lastName);

    default List<PersonEntity> findByLastNamePaged(String lastName, int page, int size) {
        return findByLastNameIgnoreCase(lastName, PageRequest.of(page, size));
    }

    List<PersonEntity> findByLastNameIgnoreCase(String lastName, PageRequest pageRequest);
}
