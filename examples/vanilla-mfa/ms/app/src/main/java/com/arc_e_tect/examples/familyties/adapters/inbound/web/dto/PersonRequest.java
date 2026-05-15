package com.arc_e_tect.examples.familyties.adapters.inbound.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PersonRequest {
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9\\s'-]+$", message = "First name contains invalid characters")
    private String firstName;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9\\s'-]+$", message = "Last name contains invalid characters")
    private String lastName;
}
