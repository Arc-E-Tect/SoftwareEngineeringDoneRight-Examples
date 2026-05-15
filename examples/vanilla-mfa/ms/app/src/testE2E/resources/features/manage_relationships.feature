Feature: Manage Relationships in Family Ties
  As a user of the Family Ties service
  I want to manage relationships between persons
  So that I can track family connections

  Scenario: Add a parent-child relationship
    Given the person with first name "John" and lastname "Smith" is already known to Family Ties
    And the person with first name "Jane" and lastname "Smith" is already known to Family Ties
    When I record a "parent" relationship from "John Smith" to "Jane Smith"
    Then the relationship is recorded successfully

  Scenario: Add a spouse relationship
    Given the person with first name "Bob" and lastname "Johnson" is already known to Family Ties
    And the person with first name "Alice" and lastname "Williams" is already known to Family Ties
    When I record a "spouse" relationship from "Bob Johnson" to "Alice Williams"
    Then the relationship is recorded successfully

  Scenario: Add a grandparent relationship
    Given the person with first name "Clara" and lastname "Bourne" is already known to Family Ties
    And the person with first name "Emma" and lastname "Smith" is already known to Family Ties
    When I record a "grandparent" relationship from "Clara Bourne" to "Emma Smith"
    Then the relationship is recorded successfully

  Scenario: Prevent adding more than two parents
    Given the person with first name "Lily" and lastname "Smith" is already known to Family Ties
    And the person with first name "Michael" and lastname "Smith" is already known to Family Ties
    And the person with first name "Sarah" and lastname "Smith" is already known to Family Ties
    And the person with first name "Thomas" and lastname "Smith" is already known to Family Ties
    And a "parent" relationship exists from "Michael Smith" to "Lily Smith"
    And a "parent" relationship exists from "Sarah Smith" to "Lily Smith"
    When I record a "parent" relationship from "Thomas Smith" to "Lily Smith"
    Then I am told a person can have at most two parents

  Scenario: Prevent adding more than four grandparents
    Given the person with first name "Olivia" and lastname "Smith" is already known to Family Ties
    And the person with first name "Robert" and lastname "Smith" is already known to Family Ties
    And the person with first name "Patricia" and lastname "Smith" is already known to Family Ties
    And the person with first name "Edward" and lastname "Smith" is already known to Family Ties
    And the person with first name "Margaret" and lastname "Smith" is already known to Family Ties
    And the person with first name "William" and lastname "Smith" is already known to Family Ties
    And a "grandparent" relationship exists from "Robert Smith" to "Olivia Smith"
    And a "grandparent" relationship exists from "Patricia Smith" to "Olivia Smith"
    And a "grandparent" relationship exists from "Edward Smith" to "Olivia Smith"
    And a "grandparent" relationship exists from "Margaret Smith" to "Olivia Smith"
    When I record a "grandparent" relationship from "William Smith" to "Olivia Smith"
    Then I am told a person can have at most four grandparents

  Scenario: Prevent adding spouse when person already has spouse
    Given the person with first name "Husband" and lastname "Smith" is already known to Family Ties
    And the person with first name "Wife1" and lastname "Jones" is already known to Family Ties
    And the person with first name "Wife2" and lastname "Brown" is already known to Family Ties
    And a "spouse" relationship exists from "Husband Smith" to "Wife1 Jones"
    When I record a "spouse" relationship from "Husband Smith" to "Wife2 Brown"
    Then I am told a person can only have one spouse

  Scenario: Retrieve relationships by lastname and type
    Given the person with first name "John" and lastname "Smith" is already known to Family Ties
    And the person with first name "Maria" and lastname "Smith" is already known to Family Ties
    And the person with first name "Charly" and lastname "Smith" is already known to Family Ties
    And a "parent" relationship exists from "John Smith" to "Maria Smith"
    And a "parent" relationship exists from "John Smith" to "Charly Smith"
    When I look up "parent" relationships for lastname "Smith"
    Then I see related people including "Maria Smith"
    And I see related people including "Charly Smith"

  Scenario: Retrieve spouse relationship
    Given the person with first name "John" and lastname "Smith" is already known to Family Ties
    And the person with first name "Ellen" and lastname "Smith" is already known to Family Ties
    And a "spouse" relationship exists from "John Smith" to "Ellen Smith"
    When I look up "spouse" relationships for lastname "Smith"
    Then I see related people including "Ellen Smith"

  Scenario: Retrieve grandparent relationship
    Given the person with first name "Clara" and lastname "Bourne" is already known to Family Ties
    And the person with first name "John" and lastname "Smith" is already known to Family Ties
    And a "grandparent" relationship exists from "Clara Bourne" to "John Smith"
    When I look up "grandparent" relationships for lastname "Bourne"
    Then I see related people including "John Smith"

  Scenario: Add relationship when person does not exist
    Given the person with first name "John" and lastname "Smith" is already known to Family Ties
    And the person with first name "Missing" and lastname "Person" is not yet known to Family Ties
    When I record a "parent" relationship from "John Smith" to "Missing Person"
    Then I am told the person does not exist

  Scenario: Retrieve relationships when no persons with lastname exist
    Given no persons with lastname "Ghost" are known to Family Ties
    When I look up "parent" relationships for lastname "Ghost"
    Then I am told no people are known with lastname "Ghost"
