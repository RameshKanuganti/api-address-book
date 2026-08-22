package com.reece.addressbook.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.reece.addressbook.dto.AddressBookDto;
import com.reece.addressbook.dto.ContactDto;
import com.reece.addressbook.exception.GlobalExceptionHandler;
import com.reece.addressbook.model.AddressBookType;
import com.reece.addressbook.service.AddressBookService;
import com.reece.addressbook.service.ContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AddressBookControllerTest {

    @Mock
    private AddressBookService addressBookService;

    @Mock
    private ContactService contactService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(
                        new AddressBookController(addressBookService, contactService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void createAddressBookReturnsCreated() throws Exception {
        AddressBookDto request = new AddressBookDto();
        request.setBranchManager("Manager One");
        request.setType(AddressBookType.DOMESTIC);

        AddressBookDto response = new AddressBookDto();
        response.setId(1L);
        response.setBranchManager("Manager One");
        response.setType(AddressBookType.DOMESTIC);

        when(addressBookService.createAddressBook(any(AddressBookDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/address-books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.branchManager").value("Manager One"))
                .andExpect(jsonPath("$.type").value("DOMESTIC"));

        verify(addressBookService).createAddressBook(any(AddressBookDto.class));
    }

    @Test
    void createAddressBookWithBlankManagerReturnsBadRequest() throws Exception {
        String payload = "{\"branchManager\":\"\",\"type\":\"DOMESTIC\",\"contacts\":[]}";

        mockMvc.perform(post("/api/v1/address-books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("branchManager Branch manager is required"));
    }

    @Test
    void createAddressBookWithInvalidNestedContactReturnsBadRequest() throws Exception {
        String payload = "{\"branchManager\":\"Manager\",\"type\":\"DOMESTIC\",\"contacts\":[{\"name\":\"\",\"phoneNumber\":\"+61412345678\"}]}";

        mockMvc.perform(post("/api/v1/address-books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("contacts[].name Contact name is required"));
    }

    @Test
    void getAllAddressBooksReturnsOk() throws Exception {
        AddressBookDto addressBook = new AddressBookDto();
        addressBook.setId(1L);
        addressBook.setBranchManager("Manager One");
        addressBook.setType(AddressBookType.CIVIL);

        when(addressBookService.getAllAddressBooks()).thenReturn(List.of(addressBook));

        mockMvc.perform(get("/api/v1/address-books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].branchManager").value("Manager One"))
                .andExpect(jsonPath("$[0].type").value("CIVIL"));
    }

    @Test
    void getAddressBookByIdReturnsOk() throws Exception {
        AddressBookDto addressBook = new AddressBookDto();
        addressBook.setId(1L);
        addressBook.setBranchManager("Manager One");
        addressBook.setType(AddressBookType.INDUSTRIAL);

        when(addressBookService.getAddressBookById(1L)).thenReturn(addressBook);

        mockMvc.perform(get("/api/v1/address-books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.branchManager").value("Manager One"))
                .andExpect(jsonPath("$.type").value("INDUSTRIAL"));
    }

    @Test
    void deleteAddressBookReturnsNoContent() throws Exception {
        doNothing().when(addressBookService).deleteAddressBook(1L);

        mockMvc.perform(delete("/api/v1/address-books/1"))
                .andExpect(status().isNoContent());

        verify(addressBookService).deleteAddressBook(1L);
    }

    @Test
    void addNewContactToAddressBookReturnsCreated() throws Exception {
        ContactDto request = new ContactDto();
        request.setName("Jane Doe");
        request.setPhoneNumber("+61123456789");

        ContactDto response = new ContactDto();
        response.setId(10L);
        response.setName("Jane Doe");
        response.setPhoneNumber("+61123456789");

        when(addressBookService.addNewContactToAddressBook(eq(1L), any(ContactDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/address-books/1/contacts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Jane Doe"))
                .andExpect(jsonPath("$.phoneNumber").value("+61123456789"));
    }

    @Test
    void removeContactFromAddressBookReturnsOk() throws Exception {
        AddressBookDto response = new AddressBookDto();
        response.setId(1L);
        response.setBranchManager("Manager One");
        response.setType(AddressBookType.FINANCIAL);

        when(addressBookService.removeContactFromAddressBook(1L, 10L)).thenReturn(response);

        mockMvc.perform(delete("/api/v1/address-books/1/contacts/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.branchManager").value("Manager One"))
                .andExpect(jsonPath("$.type").value("FINANCIAL"));
    }

    @Test
    void getUniqueContactsAcrossAddressBooksReturnsOk() throws Exception {
        ContactDto contact = new ContactDto();
        contact.setId(5L);
        contact.setName("Jane Doe");
        contact.setPhoneNumber("+61123456789");

        when(addressBookService.getUniqueContactsAcrossAddressBooks(List.of(1L, 2L)))
                .thenReturn(Set.of(contact));

        mockMvc.perform(get("/api/v1/address-books/contacts/unique")
                        .param("addressBookIds", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].name").value("Jane Doe"))
                .andExpect(jsonPath("$[0].phoneNumber").value("+61123456789"));

        verify(addressBookService).getUniqueContactsAcrossAddressBooks(List.of(1L, 2L));
    }

    @Test
    void getUniqueContactsWithoutIdsReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/address-books/contacts/unique"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Address book IDs parameter is required and cannot be empty"));
    }

    @Test
    void getAllContactsReturnsPagedResults() throws Exception {
        ContactDto contact = new ContactDto();
        contact.setId(7L);
        contact.setName("John Smith");
        contact.setPhoneNumber("+61400000000");

        Page<ContactDto> page = new PageImpl<>(List.of(contact), PageRequest.of(0, 4), 1);
        when(contactService.getAllContacts(0, 4)).thenReturn(page);

        mockMvc.perform(get("/api/v1/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(7))
                .andExpect(jsonPath("$.content[0].name").value("John Smith"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}

