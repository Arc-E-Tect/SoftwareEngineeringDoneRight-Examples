package com.arc_e_tect.book.sedr.familyties.adapters.inbound.web.dto;

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
