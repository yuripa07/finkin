package com.finkin.infrastructure.config;

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

    @Bean
    public OpenAPI finkinOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
            .info(new Info()
                .title("Finkin Bank API")
                .version("1.0.0-fase1")
                .description("""
                    API do Finkin Bank — banco digital simulado para PDI em Open Finance Brasil.

                    Todas as transações são fictícias. As regras de negócio seguem as normas do
                    Banco Central do Brasil (BCB), incluindo:
                    - Resolução BCB nº 1/2020 (Pix e limites operacionais)
                    - Resolução BCB nº 6/2020 (ISPB e identificação de instituições)

                    Autenticação: Bearer JWT — use /auth/login para obter o token.
                    """)
                .contact(new Contact().name("Finkin PDI").email("pdi@finkin.dev"))
                .license(new License().name("MIT")))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
