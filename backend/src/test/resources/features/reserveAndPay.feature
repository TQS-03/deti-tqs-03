Feature: Non-worker user registration, booking and payment flow

  As a non-worker user
  I want to create an account, book a charging station, and pay for my reservation
  So that I can use the charging service end-to-end

  Background:
    Given I am on the home page

  Scenario: Register as non-worker, book a station and pay for it
    # Navigate to registration
    When I click on the "Interactive Map" link in the navigation bar
    Then As i do not have an account i should be on Login Page

    When I click on the "Register here" link
    And I fill in the registration form with:
      | firstName       | Alice                   |
      | lastName        | Smith                   |
      | email           | alice.smith@example.com |
      | password        | P@ssw0rd                |
      | confirmPassword | P@ssw0rd                |
      | isWorker        | false                   |
    And I submit the registration form
    Then I should be redirected to the Login page

    When I fill in the login form with email "alice.smith@example.com" and password "P@ssw0rd"
    And I submit the login form
    Then I should be on the Map page

    When I select "Test Station" from the station dropdown
    Then I select the station "Test Station" on the map
    And I click the "Book" button
    Then the booking modal should open

    When I fill booking dates start "301220250900p" and end "301220251030p"
    And I confirm the booking
    Then I should see an alert with "Reservation created successfully"

    Given I am on the My Bookings page
    When I click Pay Now for my booking
    Then the payment modal should open

    When I fill payment form with:
      | energyUsed | 20.0                 |
      | cardNumber | 4222222222222  |
      | expiryDate | 12/25              |
      | cvv        | 123                |
      | saveCard   | false               |
    And I submit the payment
    Then I should see "Payment and consumption recorded successfully" and status Paid