package com.nutrifit.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nutriFitOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NutriFit API")
                        .description("API REST para gestión de alimentos, comidas y resumen nutricional diario")
                        .version("0.1.0"));
    }
}
