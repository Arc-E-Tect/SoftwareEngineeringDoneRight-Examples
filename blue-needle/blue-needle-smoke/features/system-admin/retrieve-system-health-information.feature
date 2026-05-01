Feature: Retrieve IFF Health Information

  Scenario: Retrieve IFF health information while it is running
    Given the application is deployed
    And the application is up and running
    When health information is retrieved
    Then the application is operational
