package com.sanmartin.farmacias.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI custoOpenAPI(){
        return (new OpenAPI()).info((new Info()).title("API Farmacia San Martin")
                .version("1.0").description("Descripcion de las rutas creadas"));
    }
}
