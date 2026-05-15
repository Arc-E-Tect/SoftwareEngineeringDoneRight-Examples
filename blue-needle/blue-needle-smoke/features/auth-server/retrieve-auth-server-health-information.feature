Feature: Retrieve Auth Server Health Information

  Scenario: Retrieve auth server health information while it is running
    Given the auth server service is deployed
    And the auth server service is up and running
    When the auth server health information is retrieved
    Then the auth server service is operational
