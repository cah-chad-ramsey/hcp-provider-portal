package com.sonexus.portal.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for API documentation.
 * Access at: http://localhost:8080/swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HCP Provider Portal API")
                        .version("1.0.0")
                        .description("""
                                Healthcare Provider Portal API for Sonexus Support.

                                ## Features
                                - Provider affiliation management
                                - Patient enrollment and management
                                - Benefits investigation
                                - Forms and resources
                                - Secure messaging
                                - Admin console

                                ## Authentication
                                All endpoints require JWT authentication.
                                Use the /api/v1/auth/login endpoint to obtain a token.
                                """)
                        .contact(new Contact()
                                .name("Sonexus Support")
                                .email("support@sonexus.com")
                                .url("https://sonexus.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://sonexus.com/terms")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development"),
                        new Server()
                                .url("https://api.sonexus.com")
                                .description("Production")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from /api/v1/auth/login")));
    }
}
