package com.techrepair.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI techRepairOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TechRepair API - Gestão de Assistência Técnica")
                        .description("API RESTful completa para gerenciamento de clientes, equipamentos, catálogo de peças, controle de estoque, ordens de serviço com máquina de estados e orçamento automatizado.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Alisson Paz Lopes")
                                .url("https://github.com/AlissonPazLopes/techrepair-api"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Insira o token JWT obtido no endpoint POST /api/auth/login")));
    }
}
