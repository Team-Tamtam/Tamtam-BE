package tamtam.mooney.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "Mooney API", version = "v1"),
        servers = {
                @Server(url = "/", description = "Server URL")
        })
@RequiredArgsConstructor
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI();
    }

    @Bean
    public GroupedOpenApi apiGroup() {
        String[] paths = {"/**"};

        return GroupedOpenApi.builder()
                .group("service-api-group")
                .pathsToMatch(paths)
                .build();
    }
}