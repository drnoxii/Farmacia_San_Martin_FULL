package com.sanmartin.farmacias.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI custoOpenAPI() {

        return new OpenAPI()
                // Información de nuestra API
                .info(new Info()
                        .title("API Farmacia San Martin")
                        .version("1.0")
                        .description("Descripcion de las rutas creadas")
                )
                // Configuración JWT Bearer
                .components(new Components()
                        .addSecuritySchemes(
                                "BearerAuth",
                                new SecurityScheme()
                                        .name("BearerAuth")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")

                        )




                ).addSecurityItem(
                        new SecurityRequirement().addList("BearerAuth")
                );

                
    }
}
