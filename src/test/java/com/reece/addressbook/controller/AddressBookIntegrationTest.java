package com.reece.addressbook.controller;

import com.reece.addressbook.dto.AddressBookDto;
import com.reece.addressbook.dto.ContactDto;
import com.reece.addressbook.model.AddressBookType;
import com.reece.addressbook.repository.AddressBookRepository;
import com.reece.addressbook.repository.ContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AddressBookIntegrationTest {

    @Autowired private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired private AddressBookRepository addressBookRepository;
    @Autowired private ContactRepository contactRepository;

    @BeforeEach
    void setUp() {
        addressBookRepository.deleteAll();
        contactRepository.deleteAll();
    }

    // ===================== DOMESTIC ADDRESS BOOK TESTS =====================

    @Test
    void domesticAddressBookWithMultipleContacts() throws Exception {
        AddressBookDto request = new AddressBookDto();
        request.setBranchManager("Nitesh");
        request.setType(AddressBookType.DOMESTIC);
        Set<ContactDto> contacts = new HashSet<>();
        contacts.add(createContact("Ramesh", "+61123456789"));
        contacts.add(createContact("Andrew", "+61234567890"));
        request.setContacts(contacts);

        MvcResult result = mockMvc.perform(post("/api/v1/address-books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        AddressBookDto response = objectMapper.readValue(result.getResponse().getContentAsString(), AddressBookDto.class);
        assertThat(response.getId()).isNotNull();
        assertThat(response.getBranchManager()).isEqualTo("Nitesh");
        assertThat(response.getType()).isEqualTo(AddressBookType.DOMESTIC);
        assertThat(response.getContacts()).hasSize(2);
    }

    @Test
    void domesticAddressBookRetrievalById() throws Exception {
        AddressBookDto addressBook = createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC,
                createContact("Ramesh", "+61123456789"), createContact("Andrew", "+61234567890"));

        mockMvc.perform(get("/api/v1/address-books/{id}", addressBook.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(addressBook.getId()))
                .andExpect(jsonPath("$.branchManager").value("Nitesh"))
                .andExpect(jsonPath("$.type").value("DOMESTIC"))
                .andExpect(jsonPath("$.contacts.length()").value(2));
    }

    @Test
    void domesticAddressBookAddNewContactScenario() throws Exception {
        AddressBookDto addressBook = createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC,
                createContact("Ramesh", "+61123456789"));
        ContactDto newContact = createContact("Andrew", "+61234567890");

        mockMvc.perform(post("/api/v1/address-books/{addressBookId}/contacts", addressBook.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newContact)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Andrew"))
                .andExpect(jsonPath("$.phoneNumber").value("+61234567890"));
    }

    @Test
    void domesticAddressBookDeleteContactScenario() throws Exception {
        AddressBookDto addressBook = createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC,
                createContact("Ramesh", "+61123456789"), createContact("Andrew", "+61234567890"));

        Long contactId = addressBook.getContacts().stream().findFirst().get().getId();

        MvcResult result = mockMvc.perform(delete("/api/v1/address-books/{addressBookId}/contacts/{contactId}",
                        addressBook.getId(), contactId))
                .andExpect(status().isOk())
                .andReturn();

        AddressBookDto response = objectMapper.readValue(result.getResponse().getContentAsString(), AddressBookDto.class);
        assertThat(response.getContacts()).hasSize(1);
    }

    @Test
    void domesticAddressBookDeletionScenario() throws Exception {
        AddressBookDto addressBook = createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC,
                createContact("Ramesh", "+61123456789"), createContact("Andrew", "+61234567890"));

        mockMvc.perform(delete("/api/v1/address-books/{id}", addressBook.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/address-books/{id}", addressBook.getId()))
                .andExpect(status().isNotFound());
    }

    // ===================== IRRIGATION ADDRESS BOOK TESTS =====================

    @Test
    void irrigationAddressBookWithThreeContacts() throws Exception {
        AddressBookDto request = new AddressBookDto();
        request.setBranchManager("Max");
        request.setType(AddressBookType.IRRIGATION);
        Set<ContactDto> contacts = new HashSet<>();
        contacts.add(createContact("Ramesh", "+61123456789"));
        contacts.add(createContact("Kavita", "+61345678912"));
        contacts.add(createContact("Travis", "+61456789123"));
        request.setContacts(contacts);

        MvcResult result = mockMvc.perform(post("/api/v1/address-books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        AddressBookDto response = objectMapper.readValue(result.getResponse().getContentAsString(), AddressBookDto.class);
        assertThat(response.getBranchManager()).isEqualTo("Max");
        assertThat(response.getContacts()).hasSize(3);
    }

    @Test
    void irrigationAddressBookRetrievalById() throws Exception {
        AddressBookDto addressBook = createAndPersistAddressBook("Max", AddressBookType.IRRIGATION,
                createContact("Ramesh", "+61123456789"), createContact("Kavita", "+61345678912"),
                createContact("Travis", "+61456789123"));

        mockMvc.perform(get("/api/v1/address-books/{id}", addressBook.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.branchManager").value("Max"))
                .andExpect(jsonPath("$.type").value("IRRIGATION"))
                .andExpect(jsonPath("$.contacts.length()").value(3));
    }

    @Test
    void irrigationAddressBookAddMultipleContactsSequentially() throws Exception {
        AddressBookDto addressBook = createAndPersistAddressBook("Max", AddressBookType.IRRIGATION,
                createContact("Ramesh", "+61123456789"));

        mockMvc.perform(post("/api/v1/address-books/{addressBookId}/contacts", addressBook.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createContact("Kavita", "+61345678912"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Kavita"));

        mockMvc.perform(post("/api/v1/address-books/{addressBookId}/contacts", addressBook.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createContact("Travis", "+61456789123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Travis"));

        mockMvc.perform(get("/api/v1/address-books/{id}", addressBook.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contacts.length()").value(3));
    }

    @Test
    void irrigationAddressBookRemoveMiddleContact() throws Exception {
        AddressBookDto addressBook = createAndPersistAddressBook("Max", AddressBookType.IRRIGATION,
                createContact("Ramesh", "+61123456789"), createContact("Kavita", "+61345678912"),
                createContact("Travis", "+61456789123"));

        Long contactToRemove = addressBook.getContacts().stream()
                .filter(c -> c.getName().equals("Kavita")).findFirst().get().getId();

        mockMvc.perform(delete("/api/v1/address-books/{addressBookId}/contacts/{contactId}",
                        addressBook.getId(), contactToRemove))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contacts.length()").value(2));
    }

    @Test
    void irrigationAddressBookDeleteAllContacts() throws Exception {
        AddressBookDto addressBook = createAndPersistAddressBook("Max", AddressBookType.IRRIGATION,
                createContact("Ramesh", "+61123456789"), createContact("Kavita", "+61345678912"),
                createContact("Travis", "+61456789123"));

        for (ContactDto contact : addressBook.getContacts()) {
            mockMvc.perform(delete("/api/v1/address-books/{addressBookId}/contacts/{contactId}",
                            addressBook.getId(), contact.getId()))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/api/v1/address-books/{id}", addressBook.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contacts.length()").value(0));
    }

    // ===================== INDUSTRIAL ADDRESS BOOK TESTS =====================

    @Test
    void industrialAddressBookWithThreeContacts() throws Exception {
        AddressBookDto request = new AddressBookDto();
        request.setBranchManager("Jack");
        request.setType(AddressBookType.INDUSTRIAL);
        Set<ContactDto> contacts = new HashSet<>();
        contacts.add(createContact("Manisha", "+61567891234"));
        contacts.add(createContact("Olivia", "+61678912345"));
        contacts.add(createContact("Travis", "+61789123456"));
        request.setContacts(contacts);

        MvcResult result = mockMvc.perform(post("/api/v1/address-books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        AddressBookDto response = objectMapper.readValue(result.getResponse().getContentAsString(), AddressBookDto.class);
        assertThat(response.getBranchManager()).isEqualTo("Jack");
        assertThat(response.getContacts()).hasSize(3);
    }

    @Test
    void industrialAddressBookRetrievalById() throws Exception {
        AddressBookDto addressBook = createAndPersistAddressBook("Jack", AddressBookType.INDUSTRIAL,
                createContact("Manisha", "+61567891234"), createContact("Olivia", "+61678912345"),
                createContact("Travis", "+61789123456"));

        mockMvc.perform(get("/api/v1/address-books/{id}", addressBook.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.branchManager").value("Jack"))
                .andExpect(jsonPath("$.type").value("INDUSTRIAL"))
                .andExpect(jsonPath("$.contacts.length()").value(3));
    }

    @Test
    void industrialAddressBookAddNewContact() throws Exception {
        AddressBookDto addressBook = createAndPersistAddressBook("Jack", AddressBookType.INDUSTRIAL,
                createContact("Manisha", "+61567891234"));

        mockMvc.perform(post("/api/v1/address-books/{addressBookId}/contacts", addressBook.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createContact("Olivia", "+61678912345"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Olivia"));
    }

    @Test
    void industrialAddressBookDeleteContact() throws Exception {
        AddressBookDto addressBook = createAndPersistAddressBook("Jack", AddressBookType.INDUSTRIAL,
                createContact("Manisha", "+61567891234"), createContact("Olivia", "+61678912345"));

        Long contactId = addressBook.getContacts().stream().findFirst().get().getId();

        mockMvc.perform(delete("/api/v1/address-books/{addressBookId}/contacts/{contactId}",
                        addressBook.getId(), contactId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contacts.length()").value(1));
    }

    @Test
    void industrialAddressBookDeletionScenario() throws Exception {
        AddressBookDto addressBook = createAndPersistAddressBook("Jack", AddressBookType.INDUSTRIAL,
                createContact("Manisha", "+61567891234"), createContact("Olivia", "+61678912345"),
                createContact("Travis", "+61789123456"));

        mockMvc.perform(delete("/api/v1/address-books/{id}", addressBook.getId()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/v1/address-books/{id}", addressBook.getId()))
                .andExpect(status().isNotFound());
    }

    // ===================== MULTI-ADDRESS BOOK SCENARIOS =====================

    @Test
    void retrieveAllAddressBooksWithMultipleRecords() throws Exception {
        createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC,
                createContact("Ramesh", "+61123456789"), createContact("Andrew", "+61234567890"));
        createAndPersistAddressBook("Max", AddressBookType.IRRIGATION,
                createContact("Kavita", "+61345678912"), createContact("Travis", "+61456789123"));
        createAndPersistAddressBook("Jack", AddressBookType.INDUSTRIAL,
                createContact("Manisha", "+61567891234"), createContact("Olivia", "+61678912345"));

        mockMvc.perform(get("/api/v1/address-books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void retrieveAllAddressBooksWhenEmpty() throws Exception {
        mockMvc.perform(get("/api/v1/address-books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getUniqueContactsAcrossAllAddressBooks() throws Exception {
        AddressBookDto ab1 = createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC,
                createContact("Ramesh", "+61123456789"), createContact("Andrew", "+61234567890"));
        AddressBookDto ab2 = createAndPersistAddressBook("Max", AddressBookType.IRRIGATION,
                createContact("Ramesh", "+61123456789"), createContact("Kavita", "+61345678912"));
        AddressBookDto ab3 = createAndPersistAddressBook("Jack", AddressBookType.INDUSTRIAL,
                createContact("Manisha", "+61567891234"), createContact("Olivia", "+61678912345"));

        mockMvc.perform(get("/api/v1/address-books/contacts/unique")
                        .param("addressBookIds",
                                ab1.getId().toString(),
                                ab2.getId().toString(),
                                ab3.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5)); // Ramesh shared → 5 unique
    }

    @Test
    void getUniqueContactsForSpecificAddressBooks() throws Exception {
        AddressBookDto ab1 = createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC,
                createContact("Ramesh", "+61123456789"), createContact("Andrew", "+61234567890"));
        createAndPersistAddressBook("Max", AddressBookType.IRRIGATION,
                createContact("Ramesh", "+61123456789"), createContact("Kavita", "+61345678912"));

        mockMvc.perform(get("/api/v1/address-books/contacts/unique")
                        .param("addressBookIds", ab1.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getUniqueContactsWithEmptyAddressBookIdList() throws Exception {
        createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC,
                createContact("Ramesh", "+61123456789"));

        mockMvc.perform(get("/api/v1/address-books/contacts/unique"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUniqueContactsWithNonExistentAddressBookId() throws Exception {
        mockMvc.perform(get("/api/v1/address-books/contacts/unique")
                        .param("addressBookIds", "999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllContactsAcrossAddressBooks() throws Exception {
        createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC,
                createContact("Ramesh", "+61123456789"), createContact("Andrew", "+61234567890"));
        createAndPersistAddressBook("Max", AddressBookType.IRRIGATION,
                createContact("Kavita", "+61345678912"), createContact("Travis", "+61456789123"));

        mockMvc.perform(get("/api/v1/contacts").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.totalElements").value(4));
    }

    @Test
    void getAllContactsWhenNoAddressBooksExist() throws Exception {
        mockMvc.perform(get("/api/v1/contacts").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getAllContactsWithPagination() throws Exception {
        createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC,
                createContact("Ramesh", "+61123456789"), createContact("Andrew", "+61234567890"));
        createAndPersistAddressBook("Max", AddressBookType.IRRIGATION,
                createContact("Kavita", "+61345678912"), createContact("Travis", "+61456789123"));

        mockMvc.perform(get("/api/v1/contacts").param("page", "0").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    // ===================== EDGE CASES AND ERROR SCENARIOS =====================

    @Test
    void getAddressBookByInvalidId() throws Exception {
        mockMvc.perform(get("/api/v1/address-books/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAddressBookByInvalidId() throws Exception {
        mockMvc.perform(delete("/api/v1/address-books/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeContactFromInvalidAddressBook() throws Exception {
        mockMvc.perform(delete("/api/v1/address-books/999999/contacts/123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeInvalidContactFromAddressBook() throws Exception {
        AddressBookDto addressBook = createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC,
                createContact("Ramesh", "+61123456789"));

        mockMvc.perform(delete("/api/v1/address-books/{addressBookId}/contacts/999999",
                        addressBook.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeContactNotInAddressBook() throws Exception {
        AddressBookDto ab1 = createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC,
                createContact("Ramesh", "+61123456789"));
        AddressBookDto ab2 = createAndPersistAddressBook("Max", AddressBookType.IRRIGATION,
                createContact("Andrew", "+61234567890"));

        Long contactInAb2 = ab2.getContacts().stream().findFirst().get().getId();

        // Contact belongs to ab2, not ab1 – should return 400
        mockMvc.perform(delete("/api/v1/address-books/{addressBookId}/contacts/{contactId}",
                        ab1.getId(), contactInAb2))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAddressBookWithoutContacts() throws Exception {
        AddressBookDto request = new AddressBookDto();
        request.setBranchManager("TestManager");
        request.setType(AddressBookType.CIVIL);
        request.setContacts(new HashSet<>());

        MvcResult result = mockMvc.perform(post("/api/v1/address-books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        AddressBookDto response = objectMapper.readValue(result.getResponse().getContentAsString(), AddressBookDto.class);
        assertThat(response.getContacts()).isEmpty();
    }

    @Test
    void createAddressBookWithAllAddressBookTypes() throws Exception {
        int ordinal = 0;
        for (AddressBookType type : AddressBookType.values()) {
            AddressBookDto request = new AddressBookDto();
            request.setBranchManager("Manager_" + type.name());
            request.setType(type);
            request.setContacts(new HashSet<>(Arrays.asList(
                    createContact("Contact1", "+611" + String.format("%08d", ordinal++)))));

            mockMvc.perform(post("/api/v1/address-books")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.type").value(type.name()));
        }
    }

    @Test
    void addSameContactToMultipleAddressBooks() throws Exception {
        AddressBookDto ab1 = createAndPersistAddressBook("Manager1", AddressBookType.DOMESTIC,
                createContact("SharedContact", "+61111111111"));
        AddressBookDto ab2 = createAndPersistAddressBook("Manager2", AddressBookType.IRRIGATION);

        mockMvc.perform(post("/api/v1/address-books/{addressBookId}/contacts", ab2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createContact("SharedContact", "+61111111111"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/contacts").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void deleteSingleContactFromMultipleAddressBooksScenario() throws Exception {
        ContactDto sharedContact = createContact("SharedContact", "+61111111111");
        AddressBookDto ab1 = createAndPersistAddressBook("Manager1", AddressBookType.DOMESTIC,
                sharedContact, createContact("Contact2", "+61222222222"));
        AddressBookDto ab2 = createAndPersistAddressBook("Manager2", AddressBookType.IRRIGATION,
                sharedContact, createContact("Contact3", "+61333333333"));

        Long contactId = ab1.getContacts().stream()
                .filter(c -> c.getName().equals("SharedContact")).findFirst().get().getId();

        mockMvc.perform(delete("/api/v1/address-books/{addressBookId}/contacts/{contactId}",
                        ab1.getId(), contactId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/address-books/{id}", ab2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contacts.length()").value(2));
    }

    @Test
    void getUniqueContactsWithMultipleAddressBookIds() throws Exception {
        AddressBookDto ab1 = createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC,
                createContact("Ramesh", "+61123456789"), createContact("Andrew", "+61234567890"));
        AddressBookDto ab2 = createAndPersistAddressBook("Max", AddressBookType.IRRIGATION,
                createContact("Kavita", "+61345678912"), createContact("Travis", "+61456789123"));
        createAndPersistAddressBook("Jack", AddressBookType.INDUSTRIAL,
                createContact("Manisha", "+61567891234"));

        mockMvc.perform(get("/api/v1/address-books/contacts/unique")
                        .param("addressBookIds",
                                ab1.getId().toString(),
                                ab2.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void addressBookTypeEnumContainsAllTypes() {
        assertThat(AddressBookType.values()).hasSize(6);
        assertThat(AddressBookType.values()).contains(
                AddressBookType.DOMESTIC, AddressBookType.CIVIL, AddressBookType.INDUSTRIAL,
                AddressBookType.FINANCIAL, AddressBookType.INFRASTRUCTURE, AddressBookType.IRRIGATION);
    }

    @Test
    void contactRetrievalIndependently() throws Exception {
        createAndPersistAddressBook("Manager", AddressBookType.DOMESTIC,
                createContact("Contact1", "+61111111111"));

        mockMvc.perform(get("/api/v1/contacts").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Contact1"))
                .andExpect(jsonPath("$.content[0].phoneNumber").value("+61111111111"));
    }

    // ===================== HELPER METHODS =====================

    private ContactDto createContact(String name, String phoneNumber) {
        ContactDto contact = new ContactDto();
        contact.setName(name);
        contact.setPhoneNumber(phoneNumber);
        return contact;
    }

    private AddressBookDto createAndPersistAddressBook(String branchManager, AddressBookType type,
                                                       ContactDto... contacts) throws Exception {
        AddressBookDto request = new AddressBookDto();
        request.setBranchManager(branchManager);
        request.setType(type);
        request.setContacts(new HashSet<>(Arrays.asList(contacts)));

        MvcResult result = mockMvc.perform(post("/api/v1/address-books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), AddressBookDto.class);
    }
}
