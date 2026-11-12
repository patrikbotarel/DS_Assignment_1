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
    public OpenAPI userMicroserviceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Management Microservice API")
                        .description("REST API for User Management microservice in the Energy Management System. "
                                + "Provides complete CRUD operations for User entities (Admin/Client) and "
                                + "synchronizes user accounts with the Device Microservice.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Energy Management System")
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
