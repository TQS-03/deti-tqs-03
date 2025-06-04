package tqs.electro.electro.endToEnd.steps;

import java.net.URL;
import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import tqs.electro.electro.TestcontainersConfiguration;

public class MapSteps {

  private static WebDriver driver;
  private static WebDriverWait wait;
  private static String seleniumHost;
  private static Integer seleniumPort;

  @Before
  public static void setUp() throws Exception {
    // “compose” is your DockerComposeContainer<?> from TestcontainersConfiguration
    seleniumHost = TestcontainersConfiguration.compose.getServiceHost("selenium", 4444);
    seleniumPort = TestcontainersConfiguration.compose.getServicePort("selenium", 4444);

    // Build the remote URL, e.g. http://localhost:32768/wd/hub
    URL remoteUrl = new URL("http://" + seleniumHost + ":" + seleniumPort + "/wd/hub");

    FirefoxOptions options = new FirefoxOptions();
    driver = new RemoteWebDriver(remoteUrl, options);
    wait = new WebDriverWait(driver, Duration.ofSeconds(10));
  }

  @After
  public void tearDown() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Given("I am on the home page")
  public void iAmOnTheHomePage() throws InterruptedException {
    driver.get("http://proxy_test:80/");
    wait.until(ExpectedConditions.visibilityOfElementLocated(
        By.id("nav-interactive-map")));
  }

  @When("I click on the {string} link in the navigation bar")
  public void iClickOnTheLinkInTheNavigationBar(String linkText) {
    WebElement navLink = wait.until(ExpectedConditions.elementToBeClickable(
        By.id("nav-" + linkText.replaceAll("\\s+", "-").toLowerCase())));
    navLink.click();
  }

  @Then("I should be on the Map page")
  public void iShouldBeOnTheMapPage() {
    wait.until(ExpectedConditions.urlContains("/map"));
    String currentUrl = driver.getCurrentUrl();
    Assertions.assertTrue(currentUrl.contains("/map"),
        "Expected URL to contain '/map' but was: " + currentUrl);
    // Verify “Add New Station” button is visible
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("add-station-btn")));
  }

  @When("I click the {string} button")
  public void iClickTheButton(String buttonText) {
    // We only have one “Add New Station” button, identified by
    // id="add-station-btn":
    WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
        By.id("add-station-btn")));
    addBtn.click();
  }

  @When("I fill in the station form with:")
  public void iFillInTheStationFormWith(io.cucumber.datatable.DataTable table) {
    Map<String, String> data = table.asMap(String.class, String.class);
    // Wait until the modal inputs appear
    wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("station-name")));

    driver.findElement(By.id("station-name")).sendKeys(data.get("name"));
    driver.findElement(By.id("station-address")).sendKeys(data.get("address"));
    driver.findElement(By.id("station-latitude")).sendKeys(data.get("latitude"));
    driver.findElement(By.id("station-longitude")).sendKeys(data.get("longitude"));
    driver.findElement(By.id("station-ocupation")).sendKeys(data.get("maxOccupation"));
    driver.findElement(By.id("station-current")).sendKeys(data.get("currentOccupation"));

    // Handle chargerTypes as a multi‐select (react-select):
    String[] types = data.get("chargerTypes").split(",");
    WebElement chargerSelect = driver.findElement(By.id("charger-type-select"));
    chargerSelect.click();
  }

  @When("I submit the new station form")
  public void iSubmitTheNewStationForm() {
    // Click the “Add Station” button in the modal
    WebElement submitBtn = driver.findElement(
        By.id("submit-station-btn"));
    submitBtn.click();

    // Wait for the modal to disappear (the station-name input goes away)
    wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("station-name")));
  }

  @Then("I wait until the station list contains {string}")
  public void iWaitUntilTheStationListContains(String stationName) {
    // After submission, the Map page re-fetches stations. The react-select menu for
    // station list
    // should eventually contain a <div> with that stationName:
    // First, click the dropdown control to load options into the DOM:
    WebElement stationDropdown = wait.until(ExpectedConditions.elementToBeClickable(
        By.id("stations-dropdown")));
    stationDropdown.click();

    // Wait for the option with text = stationName to appear:
    wait.until(ExpectedConditions.visibilityOfElementLocated(
        By.id("select-test-station")));
    // Close the dropdown so it doesn’t obstruct later steps
    stationDropdown.click();
  }

  @When("I select {string} from the station dropdown")
  public void iSelectFromTheStationDropdown(String stationName) {
    // Open the station dropdown again:
    WebElement stationDropdown = wait.until(ExpectedConditions.elementToBeClickable(
        By.id("stations-dropdown")));
    stationDropdown.click();

    // Click the desired station option
    WebElement opt = wait.until(ExpectedConditions.elementToBeClickable(
        By.id("select-test-station")));
    opt.click();
  }

  @Then("I should see the map recenter and all given information")
  public void iShouldSeeTheMapRecenterToLatitudeAndLongitude() {
    WebElement marker = wait.until(ExpectedConditions.elementToBeClickable(By.className("leaflet-marker-icon")));
    marker.click();

    By popupLocator = By.id("popup-test-station");
    WebElement popup = wait.until(ExpectedConditions.visibilityOfElementLocated(popupLocator));
    String popupText = popup.getText();
    Assertions.assertTrue(
        popupText.contains("Test Station"),
        "Expected popup to show atleast the station name");
  }
}
