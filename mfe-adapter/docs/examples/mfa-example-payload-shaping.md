# MFA Example - Payload Shaping implementation

These are the instructions for the MFA example in which the configuration is slightly more complex then as simple as possible.
The instructions use the instructions from the Vanilla MFA example as a basis.
In fact, it provides the same functionality as the Vanilla MFA example, with the only difference being that the MFE in the `blue-needle` project is in Dutch and the APIs provided by the MFA are in Dutch as well.
This will require the MFA to translate the incoming requests from the MFE, which are in Dutch, to the requests that the MS expects, which are in English.

This example shows how to implement payload shaping in the MFA.

The implementation must therefore be based on the Vanilla MFA example, and all changes made to the Vanilla MFA example with respect to the MFA framework found in `mfe-adapter` must be applied to the Payload-Shaping MFA implementation too.

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

## Deliverables

The Payload-Shaping implementation is expected to be created in the directory `./examples/payload-shaping-mfa` and must be based on the reference implementations of the micro-frontend and the microservice, with the MFA based on the MFA framework.
Frontend is delivered in the directory `./examples/payload-shaping-mfa/mfe`.
Backend is delivered in the directory `./examples/payload-shaping-mfa/ms`.
The MFA is delivered in the directory `./examples/payload-shaping-mfa/mfa`.
All OpenAPI descriptions are delivered in the directory `./examples/payload-shaping-mfa/openapi`.

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

#### OpenAPI
For the OpenAPI descriptions it is required to use Redocly.
The OpenAPI descriptions must be componentized following the Redocly conventions.
All APIs are published in the directory `./examples/payload-shaping-mfa/openapi`, with common components in the directory `./examples/payload-shaping-mfa/openapi/components`.
The MFA and MS specific components are in the directory `./examples/payload-shaping-mfa/openapi/components/mfa` and `./examples/payload-shaping-mfa/openapi/components/ms`.

### Persistence
For storing the data PostgreSQL is used and is to be configured as a Docker container, preferably using TestContainers.

## Architecture

The overall architecture is as follows:
The MFE connects to the MFA through a REST API.
The MFA calls the MS through REST APIs.
The MS exposes its features as REST APIs.
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
All it has to do is to translate the incoming requests and outgoing responses from the MFE to the MS, from Dutch to English.
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

#### Family Ties

The `family-ties` project already has a unit test suite and it must be extended to cover the new functionality.
It must also achieve 100% code coverage.

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

In the directory `./examples/payload-shaping-mfa` there is a `README.adoc` file that contains the documentation for the Payload-Shaping MFA that shows how to build and run the example.
The README file must be written such that it can be understood by a human, but also acts as a guide for Gen AI tools.

### Checklists

#### MFA-Review
A copy of the MFA review checklist is available in the directory `./mfe-adapter/docs` and must be copied into the directory `./examples/payload-shaping-mfa/docs`.
The review checklist must be filled out based on the Payload-Shaping MFA example.

#### Security-Review
A copy of the Security review checklist is available in the directory `./mfe-adapter/docs` and must be copied into the directory `./examples/payload-shaping-mfa/docs`.
The security review checklist must be filled out based on the Payload-Shaping MFA example.