Feature: Add and select a new charging station on the map

  As a worker of the Electro app
  I want to be able to add a new charging station via the Map page
  So that I can see it in the station dropdown and select it to recenter the map

  Background:
    Given I am on the home page

  Scenario: Add a new station and then select it
    When I click on the "Interactive Map" link in the navigation bar
    Then I should be on the Map page

    When I click the "Add New Station" button
    And I fill in the station form with:
      | name             | Test Station          |
      | address          | 123 Selenium Way      |
      | latitude         | 51.505                |
      | longitude        | -0.09                 |
      | maxOccupation    | 10                    |
      | currentOccupation| 2                     |
      | chargerTypes     | Type 1,CCS            |
    And I submit the new station form

    Then I wait until the station list contains "Test Station"
    When I select "Test Station" from the station dropdown
    Then I should see the map recenter and all given information
