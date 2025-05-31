package test.java.tqs.electro.e2e_tests;

import io.cucumber.java.en.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import pages.*;

import static org.junit.jupiter.api.Assertions.*;

public class MapSteps {
  WebDriver driver = new RemoteWebDriver(new URL(System.getenv("SELENIUM_URL")), options);; // Your WebDriver manager
  HomePage home = new HomePage(driver);
  MapPage map = new MapPage(driver);
  StationForm form = new StationForm(driver);

  @Given("I am on the home page")
  public void i_am_on_home_page() {
    driver.get(new URL(System.getenv("SELENIUM_URL"))); // Adjust based on proxy
  }

  @When("I navigate to the {string} page")
  public void i_navigate_to_page(String page) {
    home.clickNavLink(page);
  }

  @When("I click {string}")
  public void i_click_button(String buttonText) {
    map.clickAddStationButton();
  }

  @When("I fill in the station form with:")
  public void i_fill_form(io.cucumber.datatable.DataTable data) {
    form.fillForm(data.asMap(String.class, String.class));
  }

  @When("I select charger types {string}")
  public void i_select_charger_types(String types) {
    form.selectChargerTypes(types);
  }

  @When("I submit the form")
  public void i_submit_form() {
    form.submit();
  }

  @Then("I should see {string} in the station dropdown")
  public void i_should_see_in_dropdown(String stationName) {
    assertTrue(map.stationAppearsInDropdown(stationName));
  }

  @When("I select {string} from the dropdown")
  public void i_select_from_dropdown(String stationName) {
    map.selectStationFromDropdown(stationName);
  }

  @Then("the map should center on {string}")
  public void map_centers_on_station(String name) {
    assertTrue(map.isMapCenteredOn(name)); // Stubbed, implement logic if testable
  }
}
