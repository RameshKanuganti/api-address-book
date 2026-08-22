package com.reece.addressbook.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerConfigTest {

    @Test
    void addressBookOpenAPIHasExpectedMetadata() {
        OpenAPI openAPI = new SwaggerConfig().addressBookOpenAPI();

        assertThat(openAPI).isNotNull();
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Branch Manager Address Book API");
        assertThat(openAPI.getInfo().getDescription()).contains("managing customer contacts");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("1.0.0");
        assertThat(openAPI.getInfo().getContact().getName()).isEqualTo("Reece Tech Team");
        assertThat(openAPI.getInfo().getContact().getEmail()).isEqualTo("support@reece.com");
        assertThat(openAPI.getInfo().getLicense().getName()).isEqualTo("Apache 2.0");
    }
}

