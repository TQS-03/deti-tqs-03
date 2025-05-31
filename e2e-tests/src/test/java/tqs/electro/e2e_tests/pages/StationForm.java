package test.java.tqs.electro.e2e_tests.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.util.*;

public class StationForm {
  WebDriver driver;

  public StationForm(WebDriver driver) {
    this.driver = driver;
  }

  public void fillForm(Map<String, String> values) {
    setInput("station-name", values.get("name"));
    setInput("address", values.get("address"));
    setInput("latitude", values.get("latitude"));
    setInput("longitude", values.get("longitude"));
    setInput("maxOccupation", values.get("maxOccupation"));
    setInput("currentOccupation", values.get("currentOccupation"));
  }

  private void setInput(String dataTestId, String value) {
    WebElement el = driver.findElement(By.cssSelector(String.format("[data-testid='%s']", dataTestId)));
    el.clear();
    el.sendKeys(value);
  }

  public void selectChargerTypes(String types) {
    WebElement select = driver.findElement(By.cssSelector("[data-testid='charger-type-select']"));
    select.click();
    for (String type : types.split(",")) {
      driver.findElement(By.xpath(String.format("//div[contains(text(), '%s')]", type.trim()))).click();
    }
    select.sendKeys(Keys.ESCAPE); // Close the dropdown
  }

  public void submit() {
    driver.findElement(By.cssSelector("[aria-label='Submit Station']")).click();
  }
}
