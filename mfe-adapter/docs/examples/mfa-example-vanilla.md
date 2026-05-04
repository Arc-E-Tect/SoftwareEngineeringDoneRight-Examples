# MFA Example - Vanilla implementation

These are the instructions for the MFA example in which the configuration is as simple as possible, hence the Vanilla implementation.

## Documentation and references

All documentation on how to impement an MFA is available in the directory `./mfe-adapter/docs`.
In particular the document `whitepaper-micro-frontend-adapter.adoc` is relevant to understand what an MFA is and what it is expected to provide.
Use the documents in the directory `./mfe-adapter/docs` to provide guidance to the implementation.

The following projects are relevant to implementing the MFA example:

- `blue-needle`, contains a reference implementation of a micro-frontend that is connected to the MFA
- `mfe-adapter`, contains the code of the MFA framework and the documentation to implement a specific MFA.
The MFA framework is based on the Hexagonal architecture.
- `family-ties`, contains a reference implementation of a microservice that the MFA is connected to.
The microservice is based on the Hexagonal architecture.

All synchronous APIs are REST APIs and are based on the OpenAPI 3.2 specification.
All asynchronous APIs are based on the AsynchAPI 3.1 specification.

## Deliverables

The Vanilla implementation is expected to be created in the directory `./examples/vanilla-mfa` and must be based on the reference implementations of the micro-frontend and the microservice, with the MFA based on the MFA framework.
Frontend is delivered in the directory `./examples/vanilla-mfa/mfe`.
Backend is delivered in the directory `./examples/vanilla-mfa/ms`.
The MFA is delivered in the directory `./examples/vanilla-mfa/mfa`.
All OpenAPI descriptions are delivered in the directory `./examples/vanilla-mfa/openapi`.
All AsyncAPI descriptions are delivered in the directory `./examples/vanilla-mfa/asyncapi`.

## Technology

### Frontend
The `mfe` is an Angular/Typescript application and uses Angular 21. 
The `blue-needle` project is the reference for the to be created micro-frontend.
It is valid to copy the `blue-needle` project and modify it to fit our needs.
The MFE is connected to the MFA through a REST API.
Everything is based on NPM and NodeJS, and the MFE is built using the Angular CLI.

### Backend
The `ms` is a Spring Boot application based on Spring Boot 4, and Java 21.
The `family-ties` project is the reference for the to be created microservice.
It is valid to copy the `family-ties` project and modify it to fit our needs.
The MS exposes its features as REST APIs, it publishes events on a Kafka topic if and when relevant.
For the build of the project Gradle 9.5.0 is used based on the Groovy DSL.
Dependencies are managed using the `libs.versions.toml` file and the Gradle plugin `refreshVersions`.

### MFA
The `mfa` is a Spring Boot application based on Spring Boot 4 and Java 21.
The `mfe-adapter` project is the reference for the to be created MFA.
It is valid to copy the `mfe-adapter` project and modify it to fit our needs.
The MFA exposes its features as REST APIs to the MFE and calls the MS through REST APIs, it subscribes to the Kafka topic to consume the events published by the MS if and when relevant.
For the build of the project Gradle 9.5.0 is used based on the Groovy DSL.
Dependencies are managed using the `libs.versions.toml` file and the Gradle plugin `refreshVersions`.

### APIs

The project follows the API First approach.

#### OpenAPI
For the OpenAPI descriptions it is required to use Redocly.
The OpenAPI descriptions must be componentized following the Redocly conventions.
All APIs are published in the directory `./examples/vanilla-mfa/openapi`, with common components in the directory `./examples/vanilla-mfa/openapi/components`.
The MFA and MS specific components are in the directory `./examples/vanilla-mfa/openapi/components/mfa` and `./examples/vanilla-mfa/openapi/components/ms`.

#### AsyncAPI
For the AsyncAPI descriptions it is required to use AsyncAPI 3.1.
The AsyncAPI descriptions must be componentized following the AsyncAPI conventions.
All APIs are published in the directory `./examples/vanilla-mfa/asyncapi`.

### Events
Spring Cloud Bus is used to publish events to Kafka.

#### Kafka
Kafka is used for the event bus and is to be configured as a Docker container, preferably using TestContainers.

### Persistence
For storing the data PostgreSQL is used and is to be configured as a Docker container, preferably using TestContainers.

## Architecture

The overall architecture is as follows:
The MFE connects to the MFA through a REST API.
The MFA calls the MS through REST APIs, and subscribes to the Kafka topic to consume the events published by the MS.
The MS exposes its features as REST APIs, and publishes events on a Kafka topic.
The MFE is responsible for the user interface and user experience, the MFA is responsible for the orchestration and the integration between the MFE and the MS, and the MS is responsible for the business logic and the data persistence.

### Family Ties
The `family-ties` microservice already provides functionality and is based on the Hexagonal architecture.
This is the foundation for this example and its functionality should be used as-is.
The `family-ties` microservice must be updated to support the `blue-needle` micro-frontend's `health` check screen.

### Blue Needle
The `blue-needle` micro-frontend already provides some functionality but it is not based on the features exposed by the `family-ties` microservice.
The `blue-needle` micro-frontend must be updated to support the features exposed by the `family-ties` microservice.
It currently has a simple UI that shows the health of the microservice it connects to, this must be updated.

### MFA
The `mfa` reference implementation already provides functionality to forward any request to the MS, without any processing.
This is for the vanilla implementation sufficient.
The `mfa` to be implemented must forward every request from the MFE to the MS, unchallenged.
The `mfa` will not have to check for an API key, there is no need to handle a session cookie and no need to swap it with a user token.
The `mfa` will not have to handle the authentication of the user, it will not have to handle the authorization of the user, and it will not have to handle any other security aspect.
There is no need for the `mfa` to change the request or response format or headers.
The `mfa` will not have to handle any event either from the MFE or from the MS.

## Testing
Everything must be tested.

### Unit testing

#### MFA
Unit testing is mandatory for the `mfa` project and must achieve 100% code coverage.
JaCoCo is used for code coverage.

#### Family Ties

The `family-ties` project already has a unit test suite and it must be extended to cover the new functionality.
It must also achieve 100% code coverage.
JaCoCo is used for code coverage.

#### Blue Needle
The `blue-needle` project already has a unit test suite and it must be extended to cover the new functionality.
There are no code coverage requirements for the MFE.

### Component testing

The MFA and MS projects must be component tested.
Use the `family-ties` project as a reference as it already has a component test suite, but extend it to cover the new functionality.

### System testing
The three projects must be system tested.

#### Blue Needle
The MFE must be tested using Cucumber and Playwright.
There must be relevant BDD scenarios be generated using the Cucumber framework, use the `family-ties` project as a reference as it alreayd has a set of BDD scenarios.
The MFE's connection with the MFA must be mocked using Wiremock, the Wiremock server must be running as a Docker container, and mimic the MFA's behaviour based on the BDD scenarios and the OpenAPI descriptions.

#### MFA
The MFA must be tested using Cucumber and follow the same system test approach as the `family-ties` project.
Use the same BDD scenarios as the MFE project and use Wiremock to mock the MS to which the MFA is connected.
It is very likely that the same stubs can be used to mock the MS as are used in the system test of the MFE to mock the MFA.

#### Family Ties
The `family-ties` project already has a system test suite and it must be extended to cover the new functionality.

### Contract testing
The MFA and MS projects must be contract-tested.
Use the `family-ties` project as a reference as it already has a contract test suite,
The contract test must cover the complete API surface area of the MFA and MS and is based on Spring RestDocs.

#### MFA
The MFA must be contract-tested to cover the complete API surface area of the MFA and is based on Spring RestDocs.
Because the MFA connects to the MS, the MS must be mocked using Wiremock, the Wiremock server must be running as a Docker container, and mimic the MS's behavior based on the OpenAPI descriptions.
Ideally, the tests mimic at the very least the same requests as the System tests of the MFA would send to the MFA.

#### Family Ties
The MS must be contract-tested to cover the complete API surface area of the MS and is based on Spring RestDocs.
Ideally, the tests mimic at the very least the same requests as the System tests of the MS would send to the MS.

### End-to-end testing
The three projects must be end-to-end tested.
The `family-ties` project already has an end-to-end test suite and it should be used as a reference.
The end-to-end tests must test that when the MFE is used, it will connect to the MS through the MFA.
For this the MFE is tested using Cucumber and Playwright and similar if not identical BDD scenarios as used in the system test of the MFE should be used.

For the end-to-end tests the three projects must be running together containerized as Docker containers using a Docker Compose file.
The Cucumber/Playwright combination must run separately from the systems under test.
Consequently, it should be possible to start the three projects using Docker Compose and open a browser to the MFE and use the MFE to interact with the MS.

### Architecture testing
The MFA and the `family-ties` microservice must be tested for architecture compliance.
The `family-ties` project already has an architecture test suite and it should be used as a reference for the architecture test of the MFA.

## Documentation

In the directory `./examples/vanilla-mfa` there is a `README.adoc` file that contains the documentation for the Vanilla MFA that shows how to build and run the example.
The README file must be written such that it can be understood by a human, but also acts as a guide for Gen AI tools.

There is no need to copy the documentation from the reference implementations.
Only the README.adoc file must be provided.

## Additional information

It is critical that the `./blue-needle`, `./family-ties`, and `./mfe-adapter` projects are not changed as these are and must remain the reference implementations. 
All changes must be in the to be newly created subdirectories holding the example.

Because the reference implementations are based on the IFF project from the book 'Software Engineering Done Right' there can be residual references to `iff`, these can be ignored and stripped.
Also important, the `mfe-adapter` project has a framework of an MFA that is extendable by injecting functionality as needed. Keep the framework as much as possible intact. 
It is set up such that when no implementations are provided to be injected, the MFA will just skip the step in its processing pipeline. 
In other words, don't change the MFA as found in the `mfe-adapter` project, instead configure it as needed to get the required behavior. 
The guides in its `docs` directory will be helpful.

Any references to Kafka or event processing should be removed as they are not relevant to the Vanilla MFA.

## Clarifications

The following decisions and clarifications were captured during the planning phase.

### Reference implementations must remain unchanged

The `./blue-needle`, `./family-ties`, and `./mfe-adapter` projects are reference implementations and must not be modified.
All changes must go into the newly created directories under `./examples/vanilla-mfa/`.

### MFE Adapter framework: configure, do not modify

The `mfe-adapter` project contains a configurable framework that is extended via dependency injection.
When no implementation is injected for a pipeline step, the MFA skips that step automatically.
The framework pipeline classes must remain intact in the copied MFA project; vanilla behaviour is achieved by providing dedicated vanilla-specific beans (e.g. `VanillaPreAuthFilter`, `VanillaSessionStorePort`, `VanillaSecurityConfiguration`) rather than modifying framework source files.

### Health check endpoint

The MS must expose a health check endpoint (Spring Boot Actuator `GET /actuator/health` is sufficient).
The MFE must call this endpoint to display system health.
Any existing functionality that calls an IFF-specific endpoint must be discarded; the IFF endpoint is a legacy remnant and does not exist in the vanilla MS.

### JaCoCo coverage requirement

Both the MS (`examples/vanilla-mfa/ms/`) and the MFA (`examples/vanilla-mfa/mfa/`) must achieve 100% JaCoCo line coverage.
Coverage is enforced via unit tests only; integration tests or Spring context tests are not relied upon to satisfy the coverage threshold.

### MFE user interface: three tabs

The MFE user interface must reflect the Family Ties MS API groups.
There must be exactly three tabs, one per API group:

| Tab | Functionality |
|---|---|
| **Persons** | Add a person, retrieve a person by last name, delete a person by last name |
| **Relationships** | Create a relationship between persons |
| **System** | Display microservice health (calls `GET /actuator/health` through the MFA) |

### Transport and security

The vanilla example uses plain HTTP throughout (no HTTPS, no mTLS, no API key validation, no session cookies from the MFE).
Port assignments: MFE dev server on `4200`, MFA on `8080`, MS on `8081`.

### OpenAPI approach

The project follows an API-first approach: OpenAPI specifications are authored first and drive both the implementation and the WireMock stubs used by the MFE smoke tests.

All API requests and responses are HTTP requests and responses.