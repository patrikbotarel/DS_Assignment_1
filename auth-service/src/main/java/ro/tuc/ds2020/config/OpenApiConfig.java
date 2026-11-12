package ro.tuc.ds2020.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI authMicroserviceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Authorization Microservice API")
                        .description("REST API for Authentication & Authorization in the Energy Management System. "
                                + "Handles user login, token generation (JWT), registration, and token validation for Traefik Reverse Proxy.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Energy Management Team")
                                .email("admin@energyms.local")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT token obtained from Auth Microservice (/auth/login)")));
    }
}
