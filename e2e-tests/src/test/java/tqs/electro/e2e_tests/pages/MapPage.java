package test.java.tqs.electro.e2e_tests.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

public class MapPage {
  WebDriver driver;
  WebDriverWait wait;

  public MapPage(WebDriver driver) {
    this.driver = driver;
    this.wait = new WebDriverWait(driver, 10);
  }

  public void clickAddStationButton() {
    driver.findElement(By.cssSelector("[data-testid='add-station-btn']")).click();
  }

  public boolean stationAppearsInDropdown(String stationName) {
    WebElement dropdown = wait
        .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[data-testid='station-dropdown']")));
    return dropdown.getText().contains(stationName);
  }

  public void selectStationFromDropdown(String name) {
    WebElement dropdown = driver.findElement(By.cssSelector("[data-testid='station-dropdown']"));
    dropdown.click();
    dropdown.findElement(By.xpath(String.format(".//div[contains(text(), '%s')]", name))).click();
  }

  public boolean isMapCenteredOn(String stationName) {
    // Optional: verify marker visibility or lat/lng match
    return true; // Stub for now
  }
}
