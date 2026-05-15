package com.arc_e_tect.book.sedr.familyties.adapters.in.web;

import com.arc_e_tect.book.sedr.familyties.adapters.in.web.dto.ErrorResponse;
import com.arc_e_tect.book.sedr.familyties.application.common.BusinessException;
import com.arc_e_tect.book.sedr.familyties.application.common.ConflictException;
import com.arc_e_tect.book.sedr.familyties.application.common.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class RestExceptionHandlerTest {

    private RestExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new RestExceptionHandler();
        request = Mockito.mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/test");
    }

    @Test
    @DisplayName("maps NotFoundException to 404 response")
    void handlesNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(new NotFoundException("missing"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().getMessage()).isEqualTo("missing");
    }

    @Test
    @DisplayName("maps ConflictException to 409 response")
    void handlesConflict() {
        ResponseEntity<ErrorResponse> response = handler.handleConflict(new ConflictException("conflict"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody().getPath()).isEqualTo("/test");
    }

    @Test
    @DisplayName("maps validation errors to 400 response")
    void handlesValidation() {
        MethodArgumentNotValidException ex = Mockito.mock(MethodArgumentNotValidException.class);

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @DisplayName("maps BusinessException to 400 response")
    void handlesBusiness() {
        ResponseEntity<ErrorResponse> response = handler.handleBusiness(new BusinessException("business"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @DisplayName("maps ConstraintViolationException to 400 response")
    void handlesConstraintViolation() {
        ConstraintViolationException ex = Mockito.mock(ConstraintViolationException.class);
        when(ex.getMessage()).thenReturn("validation constraint violated");

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().getMessage()).isEqualTo("validation constraint violated");
    }
}
