Feature: User adds a new charging station and views it on the map

  Scenario: Add station and select it from the dropdown
    Given I am on the home page
    When I navigate to the "Map" page
    And I click "Add New Station"
    And I fill in the station form with:
      | name              | Test Station           |
      | address           | 123 Main Street        |
      | latitude          | 51.51                  |
      | longitude         | -0.1                   |
      | maxOccupation     | 10                     |
      | currentOccupation | 2                      |
    And I select charger types "Type 2, CCS"
    And I submit the form
    Then I should see "Test Station" in the station dropdown
    When I select "Test Station" from the dropdown
    Then the map should center on "Test Station"
