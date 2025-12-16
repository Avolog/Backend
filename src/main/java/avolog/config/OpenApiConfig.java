package avolog.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI todoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Todo Service API")
                        .version("v1")
                        .description("Todo CRUD, completion, and routine-linked APIs"))
                .externalDocs(new ExternalDocumentation()
                        .description("Avolog project intro")
                        .url("https://example.com"));
    }
}
