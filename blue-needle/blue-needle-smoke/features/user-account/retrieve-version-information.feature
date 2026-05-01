Feature: Retrieve User Account Service Version

  Scenario: Retrieve the service version while it is running
    Given the user account service is deployed
    When the user account service version is requested
    Then the user account version name is "UserAccount"
    And the user account version code is 1.0
