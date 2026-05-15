Feature: Retrieve System Admin Service Version

  Scenario: Retrieve the service version while it is running
    Given the service is deployed
    When the service version is requested
    Then the version name is "IFF"
    And the version code is 1.0
    And the "user-account" service is integrated
    And the "auth-server" service is integrated
    And the "relationship" service is integrated
    And the "social-network" service is integrated
