package tqs.electro.electro;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.util.Map;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    public static final DockerComposeContainer<?> compose = new DockerComposeContainer<>(
            new File("../docker-compose_test.yml"))
            .withEnv(Map.of(
                    "DB_USER", "dev",
                    "DB_PASS", "dev",
                    "GF_USER", "deti-tqs-03",
                    "GF_PASS", "deti-tqs-03",
                    "PROXY_PORT", "80"))
            .withExposedService("db", 5432)
            .withExposedService("backend", 8080, Wait.forHttp("/actuator/health").forStatusCode(200))
            .withExposedService("frontend", 3000)
            .withExposedService("proxy", 80, Wait.forHttp("/").forStatusCode(200))
            .withExposedService("selenium", 4444, Wait.forHttp("/wd/hub/status").forStatusCode(200))
            .withLocalCompose(true);

    static {
        compose.start();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        String host = compose.getServiceHost("db", 5432);
        Integer port = compose.getServicePort("db", 5432);

        registry.add("spring.datasource.url", () -> "jdbc:postgresql://" + host + ":" + port + "/Electro");
        registry.add("spring.datasource.username", () -> "dev");
        registry.add("spring.datasource.password", () -> "dev");
    }
}
