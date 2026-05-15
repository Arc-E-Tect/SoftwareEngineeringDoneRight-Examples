Feature: Retrieve Relationship Health Information

  Scenario: Retrieve relationship health information while it is running
    Given the relationship service is deployed
    And the relationship service is up and running
    When the relationship health information is retrieved
    Then the relationship service is operational
