package com.arc_e_tect.examples.familyties.adapters.in.web.dto;

import java.time.Instant;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ErrorResponse {
    Instant timestamp;
    String message;
    String path;
}
