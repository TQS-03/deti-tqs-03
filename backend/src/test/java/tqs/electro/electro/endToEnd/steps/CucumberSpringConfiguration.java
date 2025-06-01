package tqs.electro.electro.endToEnd.steps;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import tqs.electro.electro.TestcontainersConfiguration;

@CucumberContextConfiguration
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ContextConfiguration(classes = TestcontainersConfiguration.class)
public class CucumberSpringConfiguration {
}
