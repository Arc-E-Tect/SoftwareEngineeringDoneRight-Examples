@security
Feature: Enforce Security Policy
  As a security reviewer of the Family Ties service
  I want HTTP requests to be rejected
  So that all traffic uses TLS

  @RainyFlow
  Scenario: Reject person registration over http
    When I attempt to register the person with first name "Harvey" and lastname "Osborn" over http
    Then the request is rejected because http is not supported

  @RainyFlow
  Scenario: Reject person lookup over http
    When I attempt to look up family members with lastname "Secure" over http
    Then the request is rejected because http is not supported

  @RainyFlow
  Scenario: Reject person deletion over http
    When I attempt to remove the person with first name "Diana" and lastname "Shield" over http
    Then the request is rejected because http is not supported

  @RainyFlow
  Scenario: Reject relationship creation over http
    When I attempt to record a "parent" relationship from "Dana Secure" to "Eli Secure" over http
    Then the request is rejected because http is not supported

  @RainyFlow
  Scenario: Reject relationship lookup over http
    When I attempt to look up "parent" relationships for lastname "Secure" over http
    Then the request is rejected because http is not supported

  @StormyFlow
  Scenario: Reject SQL injection attempt in person firstname: DROP TABLE attack
    When I attempt to register a person with firstname "'; DROP TABLE persons; --" and lastname "Smith"
    Then the request is rejected with bad request

  @StormyFlow
  Scenario: Reject SQL injection attempt in person lastname: OR condition bypass
    When I attempt to register a person with firstname "John" and lastname "' OR '1'='1"
    Then the request is rejected with bad request

  @StormyFlow
  Scenario: Reject SQL injection attempt in person lookup: OR condition bypass
    When I attempt to look up family members with lastname "Smith' OR '1'='1"
    Then the request is rejected with bad request

  @StormyFlow
  Scenario: Reject SQL injection attempt in relationship type: DROP TABLE attack
    Given the person with first name "John" and lastname "Smith" is already known to Family Ties
    And the person with first name "Jane" and lastname "Smith" is already known to Family Ties
    When I attempt to record a relationship with type "parent'; DROP TABLE relationships; --" from "John Smith" to "Jane Smith"
    Then the request is rejected with bad request
