package com.arc_e_tect.book.sedr.familyties.adapters.outbound.persistence;

import java.util.UUID;

import com.arc_e_tect.book.sedr.familyties.application.domain.model.RelationshipType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "relationships")
@Getter
@Setter
@NoArgsConstructor
public class RelationshipEntity {
    @Id
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "from_person_id")
    private PersonEntity fromPerson;

    @ManyToOne(optional = false)
    @JoinColumn(name = "to_person_id")
    private PersonEntity toPerson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelationshipType type;
}
