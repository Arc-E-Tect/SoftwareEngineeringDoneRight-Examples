Feature: Retrieve Relationship Service Version

  Scenario: Retrieve the service version while it is running
    Given the relationship service is deployed
    When the relationship service version is requested
    Then the relationship version name is "Relationship"
    And the relationship version code is 1.0
