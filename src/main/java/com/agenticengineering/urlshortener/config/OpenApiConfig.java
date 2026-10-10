
package com.agenticengineering.urlshortener.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI urlShortenerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Agentic URL Shortener API")
                        .description(
                                "REST APIs for creating short URLs, "
                                + "redirecting users, tracking clicks, "
                                + "and viewing URL analytics."
                        )
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("URL Shortener Development Team")));
    }
}
