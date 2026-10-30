package com.sanmartin.farmacias.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Permitimos todo: /api/** e /images/**
                .allowedOrigins("http://localhost:4200") // El puerto de Angular
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATH")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
