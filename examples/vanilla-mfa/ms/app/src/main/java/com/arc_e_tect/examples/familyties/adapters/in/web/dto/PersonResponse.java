package com.arc_e_tect.examples.familyties.adapters.in.web.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PersonResponse {
    UUID id;
    String firstName;
    String lastName;
}
