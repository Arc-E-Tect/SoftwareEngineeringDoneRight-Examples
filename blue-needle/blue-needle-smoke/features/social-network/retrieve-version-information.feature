Feature: Retrieve Social Network Service Version

  Scenario: Retrieve the service version while it is running
    Given the social network service is deployed
    When the social network service version is requested
    Then the social network version name is "SocialNetwork"
    And the social network version code is 1.0
