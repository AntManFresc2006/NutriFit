package com.nutrifit.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI nutriFitOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("NutriFit API")
                        .description("API REST para gestión integral de nutrición y fitness. Permite registrar comidas, ejercicios, peso y obtener análisis personalizados")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("NutriFit")
                                .email("soporte@nutrifit.com"))
                        .license(new License()
                                .name("Licencia Propietaria")
                                .url("https://nutrifit.com/license")));
    }
}
