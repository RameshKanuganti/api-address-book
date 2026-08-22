package com.reece.addressbook.mapper;

import com.reece.addressbook.dto.ContactDto;
import com.reece.addressbook.model.Contact;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class ContactMapperTest {

    @Autowired
    private ContactMapper contactMapper;

    private Contact contact;
    private ContactDto contactDto;

    @BeforeEach
    void setUp() {
        contact = new Contact();
        contact.setId(1L);
        contact.setName("John Doe");
        contact.setPhoneNumber("+61123456789");

        contactDto = new ContactDto();
        contactDto.setId(1L);
        contactDto.setName("John Doe");
        contactDto.setPhoneNumber("+61123456789");
    }

    @Test
    void mapContactToContactDto() {
        ContactDto result = contactMapper.toContactDto(contact);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(contact.getId());
        assertThat(result.getName()).isEqualTo(contact.getName());
        assertThat(result.getPhoneNumber()).isEqualTo(contact.getPhoneNumber());
    }

    @Test
    void mapContactDtoToContact() {
        Contact result = contactMapper.toContactEntity(contactDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(contactDto.getId());
        assertThat(result.getName()).isEqualTo(contactDto.getName());
        assertThat(result.getPhoneNumber()).isEqualTo(contactDto.getPhoneNumber());
    }

    @Test
    void mapContactDtoToContactNormalizesWhitespaceInPhoneNumber() {
        ContactDto dto = new ContactDto();
        dto.setId(2L);
        dto.setName("Whitespace User");
        dto.setPhoneNumber("  +61 123 456 789  ");

        Contact result = contactMapper.toContactEntity(dto);

        assertThat(result.getPhoneNumber()).isEqualTo("+61123456789");
    }

    @Test
    void mapContactDtoToContactAllowsNullPhoneNumber() {
        ContactDto dto = new ContactDto();
        dto.setId(3L);
        dto.setName("No Phone User");
        dto.setPhoneNumber(null);

        Contact result = contactMapper.toContactEntity(dto);

        assertThat(result.getPhoneNumber()).isNull();
    }

    @Test
    void roundTripContactMapping() {
        Contact original = new Contact();
        original.setId(1L);
        original.setName("Test Contact");
        original.setPhoneNumber("+61111111111");

        ContactDto dto = contactMapper.toContactDto(original);
        Contact mapped = contactMapper.toContactEntity(dto);

        assertThat(mapped.getId()).isEqualTo(original.getId());
        assertThat(mapped.getName()).isEqualTo(original.getName());
        assertThat(mapped.getPhoneNumber()).isEqualTo(original.getPhoneNumber());
    }

    @Test
    void mapContactWithSpecialCharactersInName() {
        Contact specialContact = new Contact();
        specialContact.setId(1L);
        specialContact.setName("John O'Reilly-Smith");
        specialContact.setPhoneNumber("+61123456789");

        ContactDto result = contactMapper.toContactDto(specialContact);

        assertThat(result.getName()).isEqualTo("John O'Reilly-Smith");
    }

    @Test
    void mapContactPreservesE164PhoneNumber() {
        Contact c = new Contact();
        c.setId(5L);
        c.setName("Jane");
        c.setPhoneNumber("+61987654321");

        ContactDto result = contactMapper.toContactDto(c);

        assertThat(result.getPhoneNumber()).isEqualTo("+61987654321");
    }
}
