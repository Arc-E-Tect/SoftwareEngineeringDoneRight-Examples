package com.arc_e_tect.book.sedr.familyties.adapters.in.web;

import com.arc_e_tect.book.sedr.familyties.adapters.in.web.dto.PersonResponse;
import com.arc_e_tect.book.sedr.familyties.adapters.in.web.dto.RelationshipRequest;
import com.arc_e_tect.book.sedr.familyties.application.domain.model.Person;
import com.arc_e_tect.book.sedr.familyties.application.domain.model.Relationship;
import com.arc_e_tect.book.sedr.familyties.application.domain.model.RelationshipType;
import com.arc_e_tect.book.sedr.familyties.application.port.in.RelationshipCommandUseCase;
import com.arc_e_tect.book.sedr.familyties.application.port.in.RelationshipQueryUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/familyties/relationships")
@Validated
public class RelationshipController {

    private final RelationshipCommandUseCase commandUseCase;
    private final RelationshipQueryUseCase queryUseCase;

    public RelationshipController(RelationshipCommandUseCase commandUseCase, RelationshipQueryUseCase queryUseCase) {
        this.commandUseCase = commandUseCase;
        this.queryUseCase = queryUseCase;
    }

    @PostMapping
    public ResponseEntity<Relationship> addRelationship(@RequestBody @Valid RelationshipRequest request) {
        Relationship created = commandUseCase.addRelationship(
                request.getFromFirstName(),
                request.getFromLastName(),
                request.getToFirstName(),
                request.getToLastName(),
                RelationshipType.fromName(request.getType())
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{type}/lastnames/{lastName}")
    public ResponseEntity<List<PersonResponse>> findRelations(@PathVariable("type") @Pattern(regexp = "^[a-z]+$") String type,
                                                              @PathVariable("lastName") @Pattern(regexp = "^[a-zA-Z0-9\\s'-]+$") String lastName) {
        RelationshipType relationshipType = RelationshipType.fromName(type);
        List<Person> related = queryUseCase.findRelations(lastName, relationshipType);
        if (related.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(related.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    private PersonResponse toResponse(Person person) {
        return PersonResponse.builder()
                .id(person.getId())
                .firstName(person.getFirstName())
                .lastName(person.getLastName())
                .build();
    }
}
