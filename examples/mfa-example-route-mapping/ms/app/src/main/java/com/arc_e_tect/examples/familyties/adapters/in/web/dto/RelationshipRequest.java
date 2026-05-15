package com.arc_e_tect.examples.familyties.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RelationshipRequest {
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9\\s'-]+$", message = "From first name contains invalid characters")
    private String fromFirstName;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9\\s'-]+$", message = "From last name contains invalid characters")
    private String fromLastName;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9\\s'-]+$", message = "To first name contains invalid characters")
    private String toFirstName;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9\\s'-]+$", message = "To last name contains invalid characters")
    private String toLastName;

    @NotNull
    @Pattern(regexp = "^[a-z]+$", message = "Relationship type contains invalid characters")
    private String type;
}
