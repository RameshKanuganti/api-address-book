package com.reece.addressbook.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI addressBookOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Branch Manager Address Book API")
                        .description("REST API for managing customer contacts across multiple address books. " +
                                "Supports adding, removing, listing contacts, and retrieving unique contacts across books.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Reece Tech Team")
                                .email("support@reece.com")
                                .url("https://reece.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}
