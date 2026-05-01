package com.arc_e_tect.book.sedr.familyties.adapters.in.web;

import com.arc_e_tect.book.sedr.familyties.application.domain.model.Person;
import com.arc_e_tect.book.sedr.familyties.application.domain.model.Relationship;
import com.arc_e_tect.book.sedr.familyties.application.domain.model.RelationshipType;
import com.arc_e_tect.book.sedr.familyties.application.port.in.RelationshipCommandUseCase;
import com.arc_e_tect.book.sedr.familyties.application.port.in.RelationshipQueryUseCase;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("testcomponent")
class RelationshipControllerComponentTest {

	@Mock
	private RelationshipCommandUseCase commandUseCase;

	@Mock
	private RelationshipQueryUseCase queryUseCase;

	private MockMvc mockMvc;

	@BeforeEach
	void setup() {
		RelationshipController controller = new RelationshipController(commandUseCase, queryUseCase);
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new RestExceptionHandler())
				.build();
	}

	@Test
	@DisplayName("creates a relationship via the REST endpoint")
	void createsRelationship() throws Exception {
		Relationship created = new Relationship(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), RelationshipType.PARENT);
		given(commandUseCase.addRelationship("John", "Smith", "Jane", "Smith", RelationshipType.PARENT)).willReturn(created);

		String payload = "{" +
				"\"fromFirstName\":\"John\"," +
				"\"fromLastName\":\"Smith\"," +
				"\"toFirstName\":\"Jane\"," +
				"\"toLastName\":\"Smith\"," +
				"\"type\":\"parent\"" +
				"}";

		mockMvc.perform(post("/v1/familyties/relationships")
				.contentType(MediaType.APPLICATION_JSON)
				.content(payload))
				.andExpect(status().isCreated());

		then(commandUseCase).should().addRelationship("John", "Smith", "Jane", "Smith", RelationshipType.PARENT);
	}

	@Test
	@DisplayName("returns 404 when no relations exist")
	void returnsNotFoundWhenNoRelations() throws Exception {
		given(queryUseCase.findRelations("Smith", RelationshipType.SIBLING)).willReturn(Collections.emptyList());

		mockMvc.perform(get("/v1/familyties/relationships/sibling/lastnames/Smith")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("returns relations when present")
	void returnsRelationsWhenPresent() throws Exception {
		Person relative = new Person(UUID.randomUUID(), "John", "Smith");
		given(queryUseCase.findRelations("Smith", RelationshipType.SIBLING)).willReturn(List.of(relative));

		mockMvc.perform(get("/v1/familyties/relationships/sibling/lastnames/Smith")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].firstName").value("John"))
				.andExpect(jsonPath("$[0].lastName").value("Smith"));

		then(queryUseCase).should().findRelations("Smith", RelationshipType.SIBLING);
	}
}