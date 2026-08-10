package com.reece.addressbook.mapper;

import com.reece.addressbook.dto.ContactDto;
import com.reece.addressbook.mapper.ContactMapper;
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
        contact.setPhoneNumber(123456789L);

        contactDto = new ContactDto();
        contactDto.setId(1L);
        contactDto.setName("John Doe");
        contactDto.setPhoneNumber(123456789L);
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
    void roundTripContactMapping() {
        Contact original = new Contact();
        original.setId(1L);
        original.setName("Test Contact");
        original.setPhoneNumber(111111111L);

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
        specialContact.setPhoneNumber(123456789L);

        ContactDto result = contactMapper.toContactDto(specialContact);

        assertThat(result.getName()).isEqualTo("John O'Reilly-Smith");
    }

    @Test
    void mapContactPreservesPhoneNumberFormat() {
        Contact c = new Contact();
        c.setId(5L);
        c.setName("Jane");
        c.setPhoneNumber(9876543210L);

        ContactDto result = contactMapper.toContactDto(c);

        assertThat(result.getPhoneNumber()).isEqualTo(9876543210L);
    }
}

