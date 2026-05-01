Feature: Retrieve Social Network Health Information

  Scenario: Retrieve social network health information while it is running
    Given the social network service is deployed
    And the social network service is up and running
    When the social network health information is retrieved
    Then the social network service is operational
