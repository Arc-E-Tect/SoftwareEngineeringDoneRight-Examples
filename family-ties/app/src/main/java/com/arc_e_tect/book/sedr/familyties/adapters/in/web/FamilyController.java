package com.arc_e_tect.book.sedr.familyties.adapters.in.web;

import com.arc_e_tect.book.sedr.familyties.adapters.in.web.dto.PersonRequest;
import com.arc_e_tect.book.sedr.familyties.adapters.in.web.dto.PersonResponse;
import com.arc_e_tect.book.sedr.familyties.application.domain.model.Person;
import com.arc_e_tect.book.sedr.familyties.application.port.inbound.FamilyQueryUseCase;
import com.arc_e_tect.book.sedr.familyties.application.port.inbound.PersonCommandUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/familyties")
@Validated
public class FamilyController {

    private final PersonCommandUseCase personCommandUseCase;
    private final FamilyQueryUseCase familyQueryUseCase;

    public FamilyController(PersonCommandUseCase personCommandUseCase, FamilyQueryUseCase familyQueryUseCase) {
        this.personCommandUseCase = personCommandUseCase;
        this.familyQueryUseCase = familyQueryUseCase;
    }

    @PostMapping
    public ResponseEntity<PersonResponse> addPerson(@RequestBody @Valid PersonRequest request) {
        Person created = personCommandUseCase.addPerson(request.getFirstName(), request.getLastName());
        return ResponseEntity.created(URI.create("/v1/familyties/" + created.getLastName()))
                .body(toResponse(created));
    }

    @GetMapping("/lastnames/{lastName}")
    public ResponseEntity<List<PersonResponse>> findByLastName(@PathVariable("lastName") @Pattern(regexp = "^[a-zA-Z0-9\\s'-]+$") String lastName,
                                                                @RequestParam(value = "page", defaultValue = "0") @Min(0) int page,
                                                                @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(50) int size) {
        List<Person> persons = familyQueryUseCase.getFamilyMembers(lastName, page, size);
        if (persons.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(persons.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @DeleteMapping("/lastnames/{lastName}")
    public ResponseEntity<Void> delete(@PathVariable("lastName") @Pattern(regexp = "^[a-zA-Z0-9\\s'-]+$") String lastName,
                                       @RequestParam("firstname") @Pattern(regexp = "^[a-zA-Z0-9\\s'-]+$") String firstName) {
        personCommandUseCase.deletePerson(firstName, lastName);
        return ResponseEntity.noContent().build();
    }

    private PersonResponse toResponse(Person person) {
        return PersonResponse.builder()
                .id(person.getId())
                .firstName(person.getFirstName())
                .lastName(person.getLastName())
                .build();
    }
}
