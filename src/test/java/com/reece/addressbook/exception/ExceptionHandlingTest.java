package com.reece.addressbook.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ExceptionHandlingTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void resourceNotFoundExceptionReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/address-books/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void invalidAddressBookIdReturns404() throws Exception {
        mockMvc.perform(get("/api/v1/address-books/0"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteInvalidAddressBookReturns404() throws Exception {
        mockMvc.perform(delete("/api/v1/address-books/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeInvalidContactReturns404() throws Exception {
        mockMvc.perform(delete("/api/v1/address-books/999999/contacts/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createAddressBookWithMissingFields() throws Exception {
        String invalidJson = "{\"type\":\"DOMESTIC\"}";

        mockMvc.perform(post("/api/v1/address-books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAddressBookWithInvalidContactData() throws Exception {
        String invalidJson = "{\"branchManager\":\"Manager\",\"type\":\"DOMESTIC\",\"contacts\":[{\"name\":\"\",\"phoneNumber\":123}]}";

        mockMvc.perform(post("/api/v1/address-books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addContactWithMissingPhoneNumber() throws Exception {
        String invalidJson = "{\"name\":\"John\"}";

        mockMvc.perform(post("/api/v1/address-books/999999/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addContactWithBlankName() throws Exception {
        String invalidJson = "{\"name\":\"\",\"phoneNumber\":123456789}";

        mockMvc.perform(post("/api/v1/address-books/1/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resourceNotFoundContainsProperErrorMessage() throws Exception {
        mockMvc.perform(get("/api/v1/address-books/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void validationErrorContainsFieldInformation() throws Exception {
        String invalidJson = "{\"branchManager\":\"\",\"type\":\"DOMESTIC\",\"contacts\":[]}";

        mockMvc.perform(post("/api/v1/address-books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAddressBookWithContactMissingPhoneNumberReturnsBadRequest() throws Exception {
        String jsonPayload = "{\n" +
                "  \"branchManager\": \"Jack\",\n" +
                "  \"type\": \"INDUSTRIAL\",\n" +
                "  \"contacts\": [\n" +
                "    {\n" +
                "      \"name\": \"Manisha\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Olivia\",\n" +
                "      \"phoneNumber\": 678912345\n" +
                "    },\n" +
                "    {\n" +
                "      \"name\": \"Travis\",\n" +
                "      \"phoneNumber\": 789123456\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        mockMvc.perform(post("/api/v1/address-books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("phoneNumber")));
    }
}

