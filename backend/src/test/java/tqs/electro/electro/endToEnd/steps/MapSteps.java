package tqs.electro.electro.endToEnd.steps;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import java.util.logging.Level;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
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
    private static String baseUrl = "http://proxytest:80"; // http://proxytest:80/

    @Before
    public static void setUp() throws Exception {
        seleniumHost = TestcontainersConfiguration.compose.getServiceHost("selenium", 4444);
        seleniumPort = TestcontainersConfiguration.compose.getServicePort("selenium", 4444);

        // Build the remote URL, e.g. http://localhost:32768/wd/hub
        URL remoteUrl = new URL("http://" + seleniumHost + ":" + seleniumPort + "/wd/hub");
        LoggingPreferences logs = new LoggingPreferences();
    logs.enable(LogType.BROWSER, Level.ALL);

    FirefoxOptions options = new FirefoxOptions();
    options.setCapability("goog:loggingPrefs", logs);

    driver = new RemoteWebDriver(remoteUrl, options);
//        driver = new FirefoxDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @After
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Given("I am on the home page")
    public void iAmOnTheHomePage() throws InterruptedException {
        driver.get(baseUrl);
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("nav-interactive-map")));
    }

    @When("I click on the {string} link in the navigation bar")
    public void iClickOnTheLinkInTheNavigationBar(String linkText) {
        WebElement navLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("nav-" + linkText.replaceAll("\\s+", "-").toLowerCase())));
        navLink.click();
    }

    @Then("As i do not have an account i should be on Login Page")
    public void iShouldBeOnTheLoginPage() {
        wait.until(ExpectedConditions.urlContains("/login"));
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("/login"),
                "Expected URL to contain '/login' but was: " + currentUrl);
        // Verify login form elements are visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-password")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-submit")));
    }

    @When("I click on the {string} link")
    public void iClickOnTheLink(String linkId) {
        // linkId in Gherkin will be the visible text; we map it to our id attributes
        String idSelector;
        switch(linkId) {
            case "Register here":
                idSelector = "login-register-link";
                break;
            case "Sign in":
                idSelector = "register-login-link";
                break;
            default:
                throw new IllegalArgumentException("Unknown link: " + linkId);
        }
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(By.id(idSelector)));
        link.click();
    }

    @Then("I should be on the Register page")
    public void iShouldBeOnTheRegisterPage() {
        wait.until(ExpectedConditions.urlContains("/register"));
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("/register"),
                "Expected URL to contain '/register' but was: " + currentUrl);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("register-first-name")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("register-last-name")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("register-email")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("register-password")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("register-confirm-password")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("isWorker")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("register-submit")));
    }

    @When("I fill in the registration form with:")
    public void iFillInTheRegistrationFormWith(io.cucumber.datatable.DataTable table) throws IOException {
        Map<String, String> data = table.asMap(String.class, String.class);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("register-first-name")));

        driver.findElement(By.id("register-first-name")).sendKeys(data.get("firstName"));
        driver.findElement(By.id("register-last-name")).sendKeys(data.get("lastName"));
        driver.findElement(By.id("register-email")).sendKeys(data.get("email"));
        driver.findElement(By.id("register-password")).sendKeys(data.get("password"));
        driver.findElement(By.id("register-confirm-password")).sendKeys(data.get("confirmPassword"));

        boolean isWorker = Boolean.parseBoolean(data.get("isWorker"));
        WebElement checkbox = driver.findElement(By.id("isWorker"));
        if (checkbox.isSelected() != isWorker) {
            checkbox.click();
        }
    }

    @When("I submit the registration form")
    public void iSubmitTheRegistrationForm() throws IOException {
        WebElement submitBtn = driver.findElement(By.id("register-submit"));
        submitBtn.click();
    }

    @Then("I should be redirected to the Login page")
    public void iShouldBeRedirectedToTheLoginPage() {
        // driver.get("http://proxy_test/login");
        wait.until(ExpectedConditions.urlContains("/login"));
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("/login"),
                "Expected URL to contain '/login' but was: " + currentUrl);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email")));
    }

    @When("I fill in the login form with email {string} and password {string}")
    public void iFillInTheLoginFormWithEmailAndPassword(String email, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email")));
        driver.findElement(By.id("login-email")).sendKeys(email);
        driver.findElement(By.id("login-password")).sendKeys(password);
    }

    @When("I submit the login form")
    public void iSubmitTheLoginForm() {
        WebElement submitBtn = driver.findElement(By.id("login-submit"));
        submitBtn.click();

        System.out.println(">>> Waiting for redirect after login...");
    }

    @Then("I should be on the Map page")
    public void iShouldBeOnTheMapPage() {
        wait.until(ExpectedConditions.urlContains("/map"));
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("/map"),
                "Expected URL to contain '/map' but was: " + currentUrl);
    }

    @When("I click the {string} button")
    public void iClickTheButton(String buttonText) {
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.id(String.format("%s-station-btn", buttonText.toLowerCase()))));
        addBtn.click();
    }

    @When("I fill in the station form with:")
    public void iFillInTheStationFormWith(io.cucumber.datatable.DataTable table) {
        Map<String, String> data = table.asMap(String.class, String.class);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("station-name")));

        driver.findElement(By.id("station-name")).sendKeys(data.get("name"));
        driver.findElement(By.id("station-address")).sendKeys(data.get("address"));
        driver.findElement(By.id("station-latitude")).sendKeys(data.get("latitude"));
        driver.findElement(By.id("station-longitude")).sendKeys(data.get("longitude"));
        driver.findElement(By.id("station-ocupation")).sendKeys(data.get("maxOccupation"));
        driver.findElement(By.id("station-current")).sendKeys(data.get("currentOccupation"));

        WebElement chargerSelect = driver.findElement(By.id("charger-type-select"));
        chargerSelect.click();
    }

    @When("I submit the new station form")
    public void iSubmitTheNewStationForm() {
        WebElement submitBtn = driver.findElement(
                By.id("submit-station-btn"));
        submitBtn.click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("station-name")));
    }

    @Then("I wait until the station list contains {string}")
    public void iWaitUntilTheStationListContains(String stationName) {
        WebElement stationDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("stations-dropdown")));
        stationDropdown.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("select-test-station")));
        stationDropdown.click();
    }

    @When("I select {string} from the station dropdown")
    public void iSelectFromTheStationDropdown(String stationName) {
        // Open the station dropdown again:
        WebElement stationDropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("stations-dropdown")));
        stationDropdown.click();

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
    @Given("I am on the registration page")
    public void iAmOnTheRegistrationPage() {
        driver.get(baseUrl + "/register");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("register-first-name")));
    }

    @Then("I should see {string} and be redirected to login")
    public void iShouldSeeSuccessAndRedirectToLogin(String successMessage) {
        WebElement successAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[contains(text(),'" + successMessage + "')]")));
        Assertions.assertTrue(successAlert.isDisplayed());

        wait.until(ExpectedConditions.urlContains("/login"));
        Assertions.assertTrue(driver.getCurrentUrl().contains("/login"));
    }

    @Given("I am on the login page")
    public void iAmOnTheLoginPage() {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-email")));
    }

    @When("I select station {string} from the dropdown")
    public void iSelectStationFromDropdown(String stationLabel) {
        WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(By.id("stations-dropdown")));
        dropdown.click();
        // assume the option has id formatted: select-{stationLabel in kebab-case}
        String optId = "select-" + stationLabel.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(By.id(optId)));
        option.click();
    }

    @When("I click the Book This Station button")
    public void iClickBookThisStation() {
        // assume the popup contains a button with this exact text
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[text()='Book This Station']")));
        btn.click();
    }

    @Then("the booking modal should open")
    public void bookingModalShouldOpen() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("booking-start")));
    }

    @When("I fill booking dates start {string} and end {string}")
    public void iFillBookingDates(String start, String end) {
        WebElement startInput = driver.findElement(By.id("booking-start"));
        WebElement endInput   = driver.findElement(By.id("booking-end"));
        startInput.sendKeys(start);
        endInput.sendKeys(end);
    }

    @When("I confirm the booking")
    public void iConfirmBooking() {
        driver.findElement(By.id("confirm-booking")).click();
    }

    @Then("I should see an alert with {string}")
    public void iShouldSeeBookingSuccess(String msg) {
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        Assertions.assertTrue(alert.getText().contains(msg));
        alert.accept();
    }

    @Given("I am on the My Bookings page")
    public void iAmOnMyBookingsPage() throws IOException {
        driver.get(baseUrl + "/bookings");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("table")));
    }

    @When("I click Pay Now for my booking")
    public void iClickPayNow() {
        WebElement payLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.id("booking-pay-now")));
        payLink.click();
    }

    @Then("the payment modal should open")
    public void paymentModalShouldOpen() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("payment-energy")));
    }

    @When("I fill payment form with:")
    public void iFillPaymentForm(io.cucumber.datatable.DataTable table) {
        Map<String, String> data = table.asMap(String.class, String.class);
        driver.findElement(By.id("payment-energy")).sendKeys(data.get("energyUsed"));
        driver.findElement(By.id("payment-card-number")).sendKeys(data.get("cardNumber"));
        driver.findElement(By.id("payment-expiry-date")).sendKeys(data.get("expiryDate"));
        driver.findElement(By.id("payment-cvv")).sendKeys(data.get("cvv"));
        if (Boolean.parseBoolean(data.get("saveCard"))) {
            WebElement chk = driver.findElement(By.id("save-card"));
            if (!chk.isSelected()) {
                chk.click();
            }
        }
    }

    @When("I submit the payment")
    public void iSubmitPayment() {
        driver.findElement(By.id("payment-submit")).click();
    }

    @Then("I should see {string} and status Paid")
    public void iShouldSeePaymentSuccessAndStatus(String successMsg) {

        // verify table status column updated
        WebElement statusCell = driver.findElement(By.id("paid"));
        Assertions.assertEquals(statusCell.getText(), "Paid");
        Assertions.assertTrue(statusCell.isDisplayed());
    }

    @Then("I select the station {string} on the map")
    public void iSelectTheStationOnTheMap(String name) {
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

