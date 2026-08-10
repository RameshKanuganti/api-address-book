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
class AddressBookIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AddressBookRepository addressBookRepository;

    @Autowired
    private ContactRepository contactRepository;

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
        contacts.add(createContact("Ramesh", 123456789L));
        contacts.add(createContact("Andrew", 234567890L));
        request.setContacts(contacts);

        MvcResult result = mockMvc.perform(post("/api/v1/address-books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        AddressBookDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AddressBookDto.class);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getBranchManager()).isEqualTo("Nitesh");
        assertThat(response.getType()).isEqualTo(AddressBookType.DOMESTIC);
        assertThat(response.getContacts()).hasSize(2);
    }

    @Test
    void domesticAddressBookRetrievalById() throws Exception {
        AddressBookDto addressBook = createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC, 
            createContact("Ramesh", 123456789L), createContact("Andrew", 234567890L));

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
            createContact("Ramesh", 123456789L));

        ContactDto newContact = createContact("Andrew", 234567890L);

        MvcResult result = mockMvc.perform(post("/api/v1/address-books/{addressBookId}/contacts", 
                addressBook.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newContact)))
                .andExpect(status().isCreated())
                .andReturn();

        AddressBookDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AddressBookDto.class);

        assertThat(response.getContacts()).hasSize(2);
    }

    @Test
    void domesticAddressBookDeleteContactScenario() throws Exception {
        ContactDto contact1 = createContact("Ramesh", 123456789L);
        ContactDto contact2 = createContact("Andrew", 234567890L);
        AddressBookDto addressBook = createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC,
            contact1, contact2);

        Long contactId = addressBook.getContacts().stream().findFirst().get().getId();

        MvcResult result = mockMvc.perform(delete("/api/v1/address-books/{addressBookId}/contacts/{contactId}",
                addressBook.getId(), contactId))
                .andExpect(status().isOk())
                .andReturn();

        AddressBookDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AddressBookDto.class);

        assertThat(response.getContacts()).hasSize(1);
    }

    @Test
    void domesticAddressBookDeletionScenario() throws Exception {
        AddressBookDto addressBook = createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC,
            createContact("Ramesh", 123456789L), createContact("Andrew", 234567890L));

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
        contacts.add(createContact("Ramesh", 123456789L));
        contacts.add(createContact("Kavita", 345678912L));
        contacts.add(createContact("Travis", 456789123L));
        request.setContacts(contacts);

        MvcResult result = mockMvc.perform(post("/api/v1/address-books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        AddressBookDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AddressBookDto.class);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getBranchManager()).isEqualTo("Max");
        assertThat(response.getType()).isEqualTo(AddressBookType.IRRIGATION);
        assertThat(response.getContacts()).hasSize(3);
    }

    @Test
    void irrigationAddressBookRetrievalById() throws Exception {
        AddressBookDto addressBook = createAndPersistAddressBook("Max", AddressBookType.IRRIGATION,
            createContact("Ramesh", 123456789L), createContact("Kavita", 345678912L), 
            createContact("Travis", 456789123L));

        mockMvc.perform(get("/api/v1/address-books/{id}", addressBook.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(addressBook.getId()))
                .andExpect(jsonPath("$.branchManager").value("Max"))
                .andExpect(jsonPath("$.type").value("IRRIGATION"))
                .andExpect(jsonPath("$.contacts.length()").value(3));
    }

    @Test
    void irrigationAddressBookAddMultipleContactsSequentially() throws Exception {
        AddressBookDto addressBook = createAndPersistAddressBook("Max", AddressBookType.IRRIGATION,
            createContact("Ramesh", 123456789L));

        ContactDto contact2 = createContact("Kavita", 345678912L);
        mockMvc.perform(post("/api/v1/address-books/{addressBookId}/contacts", addressBook.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contact2)))
                .andExpect(status().isCreated());

        ContactDto contact3 = createContact("Travis", 456789123L);
        mockMvc.perform(post("/api/v1/address-books/{addressBookId}/contacts", addressBook.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contact3)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/address-books/{id}", addressBook.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contacts.length()").value(3));
    }

    @Test
    void irrigationAddressBookRemoveMiddleContact() throws Exception {
        ContactDto contact1 = createContact("Ramesh", 123456789L);
        ContactDto contact2 = createContact("Kavita", 345678912L);
        ContactDto contact3 = createContact("Travis", 456789123L);
        AddressBookDto addressBook = createAndPersistAddressBook("Max", AddressBookType.IRRIGATION,
            contact1, contact2, contact3);

        Long contactToRemove = addressBook.getContacts().stream()
                .filter(c -> c.getName().equals("Kavita")).findFirst().get().getId();

        mockMvc.perform(delete("/api/v1/address-books/{addressBookId}/contacts/{contactId}",
                addressBook.getId(), contactToRemove))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contacts.length()").value(2));
    }

    @Test
    void irrigationAddressBookDeleteAllContacts() throws Exception {
        ContactDto contact1 = createContact("Ramesh", 123456789L);
        ContactDto contact2 = createContact("Kavita", 345678912L);
        ContactDto contact3 = createContact("Travis", 456789123L);
        AddressBookDto addressBook = createAndPersistAddressBook("Max", AddressBookType.IRRIGATION,
            contact1, contact2, contact3);

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
        contacts.add(createContact("Manisha", 567891234L));
        contacts.add(createContact("Olivia", 678912345L));
        contacts.add(createContact("Travis", 789123456L));
        request.setContacts(contacts);

        MvcResult result = mockMvc.perform(post("/api/v1/address-books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        AddressBookDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AddressBookDto.class);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getBranchManager()).isEqualTo("Jack");
        assertThat(response.getType()).isEqualTo(AddressBookType.INDUSTRIAL);
        assertThat(response.getContacts()).hasSize(3);
    }

    @Test
    void industrialAddressBookRetrievalById() throws Exception {
        AddressBookDto addressBook = createAndPersistAddressBook("Jack", AddressBookType.INDUSTRIAL,
            createContact("Manisha", 567891234L), createContact("Olivia", 678912345L),
            createContact("Travis", 789123456L));

        mockMvc.perform(get("/api/v1/address-books/{id}", addressBook.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(addressBook.getId()))
                .andExpect(jsonPath("$.branchManager").value("Jack"))
                .andExpect(jsonPath("$.type").value("INDUSTRIAL"))
                .andExpect(jsonPath("$.contacts.length()").value(3));
    }

    @Test
    void industrialAddressBookAddNewContact() throws Exception {
        AddressBookDto addressBook = createAndPersistAddressBook("Jack", AddressBookType.INDUSTRIAL,
            createContact("Manisha", 567891234L));

        ContactDto newContact = createContact("Olivia", 678912345L);

        mockMvc.perform(post("/api/v1/address-books/{addressBookId}/contacts", addressBook.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newContact)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contacts.length()").value(2));
    }

    @Test
    void industrialAddressBookDeleteContact() throws Exception {
        ContactDto contact1 = createContact("Manisha", 567891234L);
        ContactDto contact2 = createContact("Olivia", 678912345L);
        AddressBookDto addressBook = createAndPersistAddressBook("Jack", AddressBookType.INDUSTRIAL,
            contact1, contact2);

        Long contactId = addressBook.getContacts().stream().findFirst().get().getId();

        mockMvc.perform(delete("/api/v1/address-books/{addressBookId}/contacts/{contactId}",
                addressBook.getId(), contactId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contacts.length()").value(1));
    }

    @Test
    void industrialAddressBookDeletionScenario() throws Exception {
        AddressBookDto addressBook = createAndPersistAddressBook("Jack", AddressBookType.INDUSTRIAL,
            createContact("Manisha", 567891234L), createContact("Olivia", 678912345L),
            createContact("Travis", 789123456L));

        mockMvc.perform(delete("/api/v1/address-books/{id}", addressBook.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/address-books/{id}", addressBook.getId()))
                .andExpect(status().isNotFound());
    }

    // ===================== MULTI-ADDRESS BOOK SCENARIOS =====================

    @Test
    void retrieveAllAddressBooksWithMultipleRecords() throws Exception {
        createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC,
            createContact("Ramesh", 123456789L), createContact("Andrew", 234567890L));
        createAndPersistAddressBook("Max", AddressBookType.IRRIGATION,
            createContact("Kavita", 345678912L), createContact("Travis", 456789123L));
        createAndPersistAddressBook("Jack", AddressBookType.INDUSTRIAL,
            createContact("Manisha", 567891234L), createContact("Olivia", 678912345L));

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
        AddressBookDto addressBook1 = createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC,
            createContact("Ramesh", 123456789L), createContact("Andrew", 234567890L));
        AddressBookDto addressBook2 = createAndPersistAddressBook("Max", AddressBookType.IRRIGATION,
            createContact("Ramesh", 123456789L), createContact("Kavita", 345678912L));
        AddressBookDto addressBook3 = createAndPersistAddressBook("Jack", AddressBookType.INDUSTRIAL,
            createContact("Manisha", 567891234L), createContact("Olivia", 678912345L));

        MvcResult result = mockMvc.perform(get("/api/v1/address-books/contacts/unique"))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();
        assertThat(jsonResponse).isNotEmpty();
    }

    @Test
    void getUniqueContactsForSpecificAddressBooks() throws Exception {
        AddressBookDto addressBook1 = createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC,
            createContact("Ramesh", 123456789L), createContact("Andrew", 234567890L));
        AddressBookDto addressBook2 = createAndPersistAddressBook("Max", AddressBookType.IRRIGATION,
            createContact("Ramesh", 123456789L), createContact("Kavita", 345678912L));

        mockMvc.perform(get("/api/v1/address-books/contacts/unique")
                .param("addressBookIds", addressBook1.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getUniqueContactsWithEmptyAddressBookIdList() throws Exception {
        createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC,
            createContact("Ramesh", 123456789L));

        mockMvc.perform(get("/api/v1/address-books/contacts/unique"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllContactsAcrossAddressBooks() throws Exception {
        createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC,
            createContact("Ramesh", 123456789L), createContact("Andrew", 234567890L));
        createAndPersistAddressBook("Max", AddressBookType.IRRIGATION,
            createContact("Kavita", 345678912L), createContact("Travis", 456789123L));

        mockMvc.perform(get("/api/v1/address-books/contacts/{pageNo}/{pageSize}", 0, 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(4))
                .andExpect(jsonPath("$.totalElements").value(4));
    }

    @Test
    void getAllContactsWhenNoAddressBooksExist() throws Exception {
        mockMvc.perform(get("/api/v1/address-books/contacts/{pageNo}/{pageSize}", 0, 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getAllContactsWithPagination() throws Exception {
        createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC,
            createContact("Ramesh", 123456789L), createContact("Andrew", 234567890L));
        createAndPersistAddressBook("Max", AddressBookType.IRRIGATION,
            createContact("Kavita", 345678912L), createContact("Travis", 456789123L));

        mockMvc.perform(get("/api/v1/address-books/contacts/{pageNo}/{pageSize}", 0, 2))
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
            createContact("Ramesh", 123456789L));

        mockMvc.perform(delete("/api/v1/address-books/{addressBookId}/contacts/999999",
                addressBook.getId()))
                .andExpect(status().isNotFound());
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

        AddressBookDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(), AddressBookDto.class);

        assertThat(response.getContacts()).isEmpty();
    }

    @Test
    void createAddressBookWithAllAddressBookTypes() throws Exception {
        for (AddressBookType type : AddressBookType.values()) {
            AddressBookDto request = new AddressBookDto();
            request.setBranchManager("Manager_" + type.name());
            request.setType(type);
            request.setContacts(new HashSet<>(Arrays.asList(
                createContact("Contact1", 100000000L + type.ordinal() * 1000L)
            )));

            mockMvc.perform(post("/api/v1/address-books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.type").value(type.name()));
        }
    }

    @Test
    void addSameContactToMultipleAddressBooks() throws Exception {
        AddressBookDto addressBook1 = createAndPersistAddressBook("Manager1", AddressBookType.DOMESTIC,
            createContact("SharedContact", 111111111L));
        AddressBookDto addressBook2 = createAndPersistAddressBook("Manager2", AddressBookType.IRRIGATION);

        ContactDto sharedContact = createContact("SharedContact", 111111111L);

        mockMvc.perform(post("/api/v1/address-books/{addressBookId}/contacts", addressBook2.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sharedContact)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/address-books/contacts/{pageNo}/{pageSize}", 0, 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void deleteSingleContactFromMultipleAddressBooksScenario() throws Exception {
        ContactDto sharedContact = createContact("SharedContact", 111111111L);
        AddressBookDto addressBook1 = createAndPersistAddressBook("Manager1", AddressBookType.DOMESTIC,
            sharedContact, createContact("Contact2", 222222222L));
        AddressBookDto addressBook2 = createAndPersistAddressBook("Manager2", AddressBookType.IRRIGATION,
            sharedContact, createContact("Contact3", 333333333L));

        Long contactId = addressBook1.getContacts().stream()
                .filter(c -> c.getName().equals("SharedContact"))
                .findFirst().get().getId();

        mockMvc.perform(delete("/api/v1/address-books/{addressBookId}/contacts/{contactId}",
                addressBook1.getId(), contactId))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/address-books/{id}", addressBook2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contacts.length()").value(2));
    }

    @Test
    void getUniqueContactsWithMultipleAddressBookIds() throws Exception {
        AddressBookDto addressBook1 = createAndPersistAddressBook("Nitesh", AddressBookType.DOMESTIC,
            createContact("Ramesh", 123456789L), createContact("Andrew", 234567890L));
        AddressBookDto addressBook2 = createAndPersistAddressBook("Max", AddressBookType.IRRIGATION,
            createContact("Kavita", 345678912L), createContact("Travis", 456789123L));
        AddressBookDto addressBook3 = createAndPersistAddressBook("Jack", AddressBookType.INDUSTRIAL,
            createContact("Manisha", 567891234L));

        mockMvc.perform(get("/api/v1/address-books/contacts/unique")
                .param("addressBookIds", addressBook1.getId().toString(), addressBook2.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void addressBookTypeEnumContainsAllTypes() {
        assertThat(AddressBookType.values()).hasSize(6);
        assertThat(AddressBookType.values()).contains(
            AddressBookType.DOMESTIC,
            AddressBookType.CIVIL,
            AddressBookType.INDUSTRIAL,
            AddressBookType.FINANCIAL,
            AddressBookType.INFRASTRUCTURE,
            AddressBookType.IRRIGATION
        );
    }

    @Test
    void contactRetrievalIndependently() throws Exception {
        AddressBookDto addressBook = createAndPersistAddressBook("Manager", AddressBookType.DOMESTIC,
            createContact("Contact1", 111111111L));

        mockMvc.perform(get("/api/v1/address-books/contacts/{pageNo}/{pageSize}", 0, 10))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Contact1"))
                .andExpect(jsonPath("$.content[0].phoneNumber").value(111111111L));
    }

    // ===================== HELPER METHODS =====================

    private ContactDto createContact(String name, Long phoneNumber) {
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

        return objectMapper.readValue(
                result.getResponse().getContentAsString(), AddressBookDto.class);
    }
}

