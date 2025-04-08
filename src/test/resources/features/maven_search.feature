Feature: Maven Search CLI Tool
  As a Java developer
  I want to search for Maven dependencies
  So that I can easily add them to my projects

  Background:
    Given the Maven Search CLI tool is installed
    And I have a working internet connection

  Scenario: Search for a specific artifact
    When I search for artifact "spring-boot-starter-parent"
    Then I should see search results
    And the results should contain "org.springframework.boot:spring-boot-starter-parent"
    And the results should show the latest version
    And the results should show the Maven dependency format

  Scenario: Search for a specific artifact with Gradle format
    When I search for artifact "junit" with format "gradle"
    Then I should see search results
    And the results should contain "junit:junit"
    And the results should show the latest version
    And the results should show the Gradle dependency format

  Scenario: Search by group ID
    When I search with group ID "org.slf4j"
    Then I should see search results
    And all results should have group ID "org.slf4j"
    And each result should show artifact details

  Scenario: Search for non-existent artifact
    When I search for artifact "non-existent-artifact-12345"
    Then I should see a message indicating no results were found

  Scenario Outline: Search with different output formats
    When I search for artifact "<artifact>" with format "<format>"
    Then I should see search results
    And the results should be in "<format>" format

    Examples:
      | artifact              | format        |
      | hibernate-validator   | maven         |
      | hibernate-validator   | gradle        |
      | hibernate-validator   | gradle-kotlin |
      | hibernate-validator   | sbt           |

  Scenario: View multiple versions of an artifact
    When I search for artifact "junit"
    Then I should see search results
    And the results should show multiple versions
    And versions should be sorted with latest first

  Scenario: Handle network connectivity issues
    Given I have no internet connection
    When I search for artifact "junit"
    Then I should see an appropriate error message
    And the error message should suggest checking network connectivity

  Scenario: Validate command line arguments
    When I execute the tool without any arguments
    Then I should see usage instructions
    And the instructions should show supported formats
    And the instructions should show example commands 