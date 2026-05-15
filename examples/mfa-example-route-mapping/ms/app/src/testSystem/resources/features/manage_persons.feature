Feature: Manage Persons in Family Ties
  As a user of the Family Ties service
  I want to manage persons in the system
  So that I can track family relationships

  @SunnyFlow
  Scenario: Add a new person to the system
    Given the person with first name "John" and lastname "Smith" is not yet known to Family Ties
    When I register the person with first name "John" and lastname "Smith"
    Then the person with first name "John" and lastname "Smith" is now registered

  @SunnyFlow
  Scenario: Add a person that already exists
    Given the person with first name "Jane" and lastname "Doe" is already known to Family Ties
    When I register the person with first name "Jane" and lastname "Doe"
    Then I am told the person already exists

  @SunnyFlow
  Scenario: Retrieve family members by lastname
    Given the following persons are known to Family Ties:
      | firstName | lastName |
      | John      | Smith    |
      | Jane      | Smith    |
      | Bob       | Smith    |
    When I look up family members with lastname "Smith"
    Then I see 3 people in the family list
    And the family list includes person "John Smith"
    And the family list includes person "Jane Smith"
    And the family list includes person "Bob Smith"

  @SunnyFlow
  Scenario: Retrieve family members when none exist
    Given no persons with lastname "Ghost" are known to Family Ties
    When I look up family members with lastname "Ghost"
    Then I am told no family members are known with lastname "Ghost"

  @SunnyFlow
  Scenario: Delete a person from the system
    Given the person with first name "Alice" and lastname "Brown" is already known to Family Ties
    When I remove the person with first name "Alice" and lastname "Brown"
    Then the person with first name "Alice" and lastname "Brown" is no longer listed

  @SunnyFlow
  Scenario: Delete a person that does not exist
    Given the person with first name "Missing" and lastname "Person" is not yet known to Family Ties
    When I remove the person with first name "Missing" and lastname "Person"
    Then I am told the person does not exist

  @SunnyFlow
  Scenario: Pagination returns first 10 persons
    Given 15 persons with lastname "BigFamily" are known to Family Ties
    When I look up family members with lastname "BigFamily"
    Then I see 10 people in the family list

  @SunnyFlow
  Scenario: Pagination with page parameter
    Given 15 persons with lastname "BigFamily" are known to Family Ties
    When I look up family members with lastname "BigFamily" with page 1 and size 10
    Then I see 5 people in the family list
