Feature: Retrieve User Account Health Information

  Scenario: Retrieve user account health information while it is running
    Given the user account service is deployed
    And the user account service is up and running
    When the user account health information is retrieved
    Then the user account service is operational
