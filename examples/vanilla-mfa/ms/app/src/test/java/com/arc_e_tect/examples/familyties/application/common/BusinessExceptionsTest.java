package com.arc_e_tect.examples.familyties.application.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BusinessExceptionsTest {

    @Test
    @DisplayName("creates NotFoundException with provided message")
    void createsNotFoundException() {
        NotFoundException ex = new NotFoundException("missing");
        assertThat(ex.getMessage()).isEqualTo("missing");
    }

    @Test
    @DisplayName("creates ConflictException with provided message")
    void createsConflictException() {
        ConflictException ex = new ConflictException("conflict");
        assertThat(ex.getMessage()).isEqualTo("conflict");
    }

    @Test
    @DisplayName("creates BusinessException with provided message")
    void createsBusinessException() {
        BusinessException ex = new BusinessException("business");
        assertThat(ex.getMessage()).isEqualTo("business");
    }
}
