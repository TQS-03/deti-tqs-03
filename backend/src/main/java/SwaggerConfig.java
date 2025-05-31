import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration
@OpenAPIDefinition(info = @Info(title = "API Documentation", version = "1.0.0", description = "API Documentation", license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html")), servers = {
    @Server(url = "http://localhost:8080", description = "Local Server")
})
public class SwaggerConfig {

}
