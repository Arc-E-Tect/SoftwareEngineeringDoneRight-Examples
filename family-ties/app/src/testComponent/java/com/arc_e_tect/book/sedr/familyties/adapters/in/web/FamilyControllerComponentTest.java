package com.arc_e_tect.book.sedr.familyties.adapters.in.web;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.arc_e_tect.book.sedr.familyties.application.port.in.FamilyQueryUseCase;
import com.arc_e_tect.book.sedr.familyties.application.port.in.PersonCommandUseCase;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("testcomponent")
class FamilyControllerComponentTest {

    @Mock
    private PersonCommandUseCase personCommandUseCase;

    @Mock
    private FamilyQueryUseCase familyQueryUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        FamilyController controller = new FamilyController(personCommandUseCase, familyQueryUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new RestExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("returns 404 when no family members are found")
    void returnsNotFoundWhenNoFamilyMembers() throws Exception {
        given(familyQueryUseCase.getFamilyMembers(anyString(), anyInt(), anyInt())).willReturn(Collections.emptyList());

        mockMvc.perform(get("/v1/familyties/lastnames/Unknown").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
