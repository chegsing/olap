package com.prueba.olap.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(new Info()
                        .title("OLAP Aggregation API")
                        .version("1.0.0")
                        .description("API para consultar agregados desde un cubo OLAP")
                        .contact(new Contact().name("Equipo OLAP").email("ops@example.com")));
    }
}
