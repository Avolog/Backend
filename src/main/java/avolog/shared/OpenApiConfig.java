package avolog.shared;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Groups Service API",
                version = "1.0.0",
                description = "그룹 서비스 REST API 문서"
        )
)
public class OpenApiConfig {
}
