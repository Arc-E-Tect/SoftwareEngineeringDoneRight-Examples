package com.arc_e_tect.book.sedr.familyties.application;

import com.arc_e_tect.book.sedr.familyties.adapters.in.web.FamilyController;
import com.arc_e_tect.book.sedr.familyties.adapters.in.web.RelationshipController;
import com.arc_e_tect.book.sedr.familyties.adapters.in.web.RestExceptionHandler;
import com.arc_e_tect.book.sedr.familyties.adapters.in.web.dto.PersonRequest;
import com.arc_e_tect.book.sedr.familyties.adapters.in.web.dto.RelationshipRequest;
import com.arc_e_tect.book.sedr.familyties.application.common.ConflictException;
import com.arc_e_tect.book.sedr.familyties.application.common.NotFoundException;
import com.arc_e_tect.book.sedr.familyties.application.domain.model.Person;
import com.arc_e_tect.book.sedr.familyties.application.domain.model.Relationship;
import com.arc_e_tect.book.sedr.familyties.application.domain.model.RelationshipType;
import com.arc_e_tect.book.sedr.familyties.application.port.in.FamilyQueryUseCase;
import com.arc_e_tect.book.sedr.familyties.application.port.in.PersonCommandUseCase;
import com.arc_e_tect.book.sedr.familyties.application.port.in.RelationshipCommandUseCase;
import com.arc_e_tect.book.sedr.familyties.application.port.in.RelationshipQueryUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith({RestDocumentationExtension.class, MockitoExtension.class})
class FamilyContractTest {

    private static final UUID JOHN_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID JANE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID RELATIONSHIP_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Mock
    private PersonCommandUseCase personCommandUseCase;

    @Mock
    private FamilyQueryUseCase familyQueryUseCase;

    @Mock
    private RelationshipCommandUseCase relationshipCommandUseCase;

    @Mock
    private RelationshipQueryUseCase relationshipQueryUseCase;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup(RestDocumentationContextProvider restDocumentation) {
    objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    mockMvc = MockMvcBuilders.standaloneSetup(
            new FamilyController(personCommandUseCase, familyQueryUseCase),
            new RelationshipController(relationshipCommandUseCase, relationshipQueryUseCase))
        .setControllerAdvice(new RestExceptionHandler())
        .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
        .apply(documentationConfiguration(restDocumentation))
        .build();
    }

    @Test
    void documentsAddPerson() throws Exception {
    Person created = new Person(JOHN_ID, "John", "Doe");
    given(personCommandUseCase.addPerson("John", "Doe")).willReturn(created);

    mockMvc.perform(post("/v1/familyties")
            .contentType(APPLICATION_JSON)
            .content(json(personRequest("John", "Doe"))))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", "/v1/familyties/Doe"))
        .andExpect(jsonPath("$.id").value(JOHN_ID.toString()))
        .andExpect(jsonPath("$.firstName").value("John"))
        .andExpect(jsonPath("$.lastName").value("Doe"))
        .andDo(document("familyties-add-person",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestFields(
                fieldWithPath("firstName").description("First name of the person"),
                fieldWithPath("lastName").description("Last name of the person")
            ),
            responseHeaders(headerWithName("Location").description("Location of the created person")),
            responseFields(
                fieldWithPath("id").description("Generated person identifier"),
                fieldWithPath("firstName").description("First name"),
                fieldWithPath("lastName").description("Last name")
            )));
    }

    @Test
    void documentsAddPersonConflict() throws Exception {
    given(personCommandUseCase.addPerson("Jane", "Doe")).willThrow(new ConflictException("Person already exists"));

    mockMvc.perform(post("/v1/familyties")
            .contentType(APPLICATION_JSON)
            .content(json(personRequest("Jane", "Doe"))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Person already exists"))
        .andDo(document("familyties-add-person-conflict",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestFields(
                fieldWithPath("firstName").description("First name of the person"),
                fieldWithPath("lastName").description("Last name of the person")
            ),
            responseFields(errorResponseFields())));
    }

    @Test
    void documentsGetFamilyMembers() throws Exception {
    Person john = new Person(JOHN_ID, "John", "Smith");
    Person jane = new Person(JANE_ID, "Jane", "Smith");
    given(familyQueryUseCase.getFamilyMembers(eq("Smith"), eq(1), eq(5))).willReturn(List.of(john, jane));

    mockMvc.perform(get("/v1/familyties/lastnames/{lastName}", "Smith")
            .param("page", "1")
            .param("size", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(JOHN_ID.toString()))
        .andExpect(jsonPath("$[1].id").value(JANE_ID.toString()))
        .andDo(document("familyties-get-family-members",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(parameterWithName("lastName").description("Family last name to search")),
            queryParameters(
                parameterWithName("page").description("Zero-based page index").optional(),
                parameterWithName("size").description("Page size (1-50)").optional()
            ),
            responseFields(
                fieldWithPath("[].id").description("Person identifier"),
                fieldWithPath("[].firstName").description("First name"),
                fieldWithPath("[].lastName").description("Last name")
            )));
    }

    @Test
    void documentsGetFamilyMembersNotFound() throws Exception {
    given(familyQueryUseCase.getFamilyMembers(eq("Unknown"), eq(0), eq(10))).willReturn(Collections.emptyList());

    mockMvc.perform(get("/v1/familyties/lastnames/{lastName}", "Unknown"))
        .andExpect(status().isNotFound())
        .andDo(document("familyties-get-family-members-not-found",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(parameterWithName("lastName").description("Family last name to search")),
            queryParameters(
                parameterWithName("page").description("Zero-based page index").optional(),
                parameterWithName("size").description("Page size (1-50)").optional()
            )));
    }

    @Test
    void documentsDeletePerson() throws Exception {
        mockMvc.perform(delete("/v1/familyties/lastnames/{lastName}", "Doe")
            .queryParam("firstname", "Jane"))
        .andExpect(status().isNoContent())
        .andDo(document("familyties-delete-person",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("lastName").description("Last name of the person")),
                        queryParameters(parameterWithName("firstname").description("First name of the person"))));
    }

    @Test
    void documentsDeletePersonNotFound() throws Exception {
    doThrow(new NotFoundException("Person not found"))
        .when(personCommandUseCase).deletePerson("Jake", "Doe");

        mockMvc.perform(delete("/v1/familyties/lastnames/{lastName}", "Doe")
            .queryParam("firstname", "Jake"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Person not found"))
        .andDo(document("familyties-delete-person-not-found",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
                        pathParameters(parameterWithName("lastName").description("Last name of the person")),
                        queryParameters(parameterWithName("firstname").description("First name of the person")),
            responseFields(errorResponseFields())));
    }

    @Test
    void documentsAddRelationship() throws Exception {
    Relationship relationship = new Relationship(RELATIONSHIP_ID, JOHN_ID, JANE_ID, RelationshipType.SPOUSE);
    given(relationshipCommandUseCase.addRelationship("John", "Doe", "Jane", "Doe", RelationshipType.SPOUSE))
        .willReturn(relationship);

    mockMvc.perform(post("/v1/familyties/relationships")
            .contentType(APPLICATION_JSON)
            .content(json(relationshipRequest("John", "Doe", "Jane", "Doe", "spouse"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(RELATIONSHIP_ID.toString()))
        .andExpect(jsonPath("$.type").value("SPOUSE"))
        .andDo(document("relationships-add",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestFields(
                fieldWithPath("fromFirstName").description("First name of the originating person"),
                fieldWithPath("fromLastName").description("Last name of the originating person"),
                fieldWithPath("toFirstName").description("First name of the related person"),
                fieldWithPath("toLastName").description("Last name of the related person"),
                fieldWithPath("type").description("Relationship type")
            ),
            responseFields(
                fieldWithPath("id").description("Relationship identifier"),
                fieldWithPath("fromPersonId").description("Origin person identifier"),
                fieldWithPath("toPersonId").description("Related person identifier"),
                fieldWithPath("type").description("Relationship type")
            )));
    }

    @Test
    void documentsAddRelationshipConflict() throws Exception {
    given(relationshipCommandUseCase.addRelationship("John", "Doe", "Jane", "Doe", RelationshipType.SPOUSE))
        .willThrow(new ConflictException("Relationship violates constraints"));

    mockMvc.perform(post("/v1/familyties/relationships")
            .contentType(APPLICATION_JSON)
            .content(json(relationshipRequest("John", "Doe", "Jane", "Doe", "spouse"))))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Relationship violates constraints"))
        .andDo(document("relationships-add-conflict",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestFields(
                fieldWithPath("fromFirstName").description("First name of the originating person"),
                fieldWithPath("fromLastName").description("Last name of the originating person"),
                fieldWithPath("toFirstName").description("First name of the related person"),
                fieldWithPath("toLastName").description("Last name of the related person"),
                fieldWithPath("type").description("Relationship type")
            ),
            responseFields(errorResponseFields())));
    }

    @Test
    void documentsGetRelationsByType() throws Exception {
    Person spouse = new Person(JANE_ID, "Jane", "Smith");
    given(relationshipQueryUseCase.findRelations("Smith", RelationshipType.SPOUSE)).willReturn(List.of(spouse));

    mockMvc.perform(get("/v1/familyties/relationships/{type}/lastnames/{lastName}", "spouse", "Smith"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(JANE_ID.toString()))
        .andDo(document("relationships-get-by-type",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("type").description("Relationship type to query"),
                parameterWithName("lastName").description("Last name for the related family")
            ),
            responseFields(
                fieldWithPath("[].id").description("Person identifier"),
                fieldWithPath("[].firstName").description("First name"),
                fieldWithPath("[].lastName").description("Last name")
            )));
    }

    @Test
    void documentsGetRelationsByTypeNotFound() throws Exception {
    given(relationshipQueryUseCase.findRelations("Unknown", RelationshipType.SPOUSE)).willReturn(Collections.emptyList());

    mockMvc.perform(get("/v1/familyties/relationships/{type}/lastnames/{lastName}", "spouse", "Unknown"))
        .andExpect(status().isNotFound())
        .andDo(document("relationships-get-by-type-not-found",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            pathParameters(
                parameterWithName("type").description("Relationship type to query"),
                parameterWithName("lastName").description("Last name for the related family")
            )));
    }

    @Test
    void documentsAddPersonSqlInjectionFirstName() throws Exception {
    mockMvc.perform(post("/v1/familyties")
            .contentType(APPLICATION_JSON)
            .content(json(personRequest("'; DROP TABLE persons; --", "Smith"))))
        .andExpect(status().isBadRequest())
        .andDo(document("familyties-add-person-sql-injection-firstname",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestFields(
                fieldWithPath("firstName").description("First name with SQL injection attempt"),
                fieldWithPath("lastName").description("Last name of the person")
            ),
            responseFields(errorResponseFields())));
    }

    @Test
    void documentsAddPersonSqlInjectionLastName() throws Exception {
    mockMvc.perform(post("/v1/familyties")
            .contentType(APPLICATION_JSON)
            .content(json(personRequest("John", "' OR '1'='1"))))
        .andExpect(status().isBadRequest())
        .andDo(document("familyties-add-person-sql-injection-lastname",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestFields(
                fieldWithPath("firstName").description("First name of the person"),
                fieldWithPath("lastName").description("Last name with SQL injection attempt")
            ),
            responseFields(errorResponseFields())));
    }

    @Test
    void documentsAddRelationshipSqlInjectionType() throws Exception {
    mockMvc.perform(post("/v1/familyties/relationships")
            .contentType(APPLICATION_JSON)
            .content(json(relationshipRequest("John", "Smith", "Jane", "Smith", "parent'; DROP TABLE relationships; --"))))
        .andExpect(status().isBadRequest())
        .andDo(document("relationships-add-sql-injection-type",
            preprocessRequest(prettyPrint()),
            preprocessResponse(prettyPrint()),
            requestFields(
                fieldWithPath("fromFirstName").description("First name of the source person"),
                fieldWithPath("fromLastName").description("Last name of the source person"),
                fieldWithPath("toFirstName").description("First name of the target person"),
                fieldWithPath("toLastName").description("Last name of the target person"),
                fieldWithPath("type").description("Relationship type with SQL injection attempt")
            ),
            responseFields(errorResponseFields())));
    }

    private PersonRequest personRequest(String firstName, String lastName) {
    PersonRequest request = new PersonRequest();
    request.setFirstName(firstName);
    request.setLastName(lastName);
    return request;
    }

    private RelationshipRequest relationshipRequest(String fromFirst, String fromLast, String toFirst, String toLast, String type) {
    RelationshipRequest request = new RelationshipRequest();
    request.setFromFirstName(fromFirst);
    request.setFromLastName(fromLast);
    request.setToFirstName(toFirst);
    request.setToLastName(toLast);
    request.setType(type);
    return request;
    }

    private FieldDescriptor[] errorResponseFields() {
    return new FieldDescriptor[]{
        fieldWithPath("timestamp").description("Time when the error was created"),
        fieldWithPath("message").description("Description of the error"),
        fieldWithPath("path").description("Request path")
    };
    }

    private String json(Object value) throws JsonProcessingException {
    return objectMapper.writeValueAsString(value);
    }
}
