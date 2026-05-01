Feature: Retrieve Auth Server Service Version

  Scenario: Retrieve the service version while it is running
    Given the auth server service is deployed
    When the auth server service version is requested
    Then the auth server version name is "AuthServer"
    And the auth server version code is 1.0
