package test.java.tqs.electro.e2e_tests;

import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {
    "pretty" }, features = "src/test/java/tqs/electro/e2e_tests/features", glue = "test.java.tqs.electro.e2e_tests")
public class RunCucumberTest {
}
