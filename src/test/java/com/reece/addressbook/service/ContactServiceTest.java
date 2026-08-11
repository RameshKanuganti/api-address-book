package com.reece.addressbook.service;

import com.reece.addressbook.dto.ContactDto;
import com.reece.addressbook.exception.ResourceNotFoundException;
import com.reece.addressbook.mapper.ContactMapper;
import com.reece.addressbook.model.Contact;
import com.reece.addressbook.repository.ContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceTest {

    @Mock
    private ContactRepository contactRepository;

    @Mock
    private ContactMapper contactMapper;

    @InjectMocks
    private ContactService contactService;

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
    void createContactSuccessfully() {
        when(contactMapper.toContactEntity(contactDto)).thenReturn(contact);
        when(contactRepository.save(contact)).thenReturn(contact);
        when(contactMapper.toContactDto(contact)).thenReturn(contactDto);

        ContactDto result = contactService.create(contactDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getPhoneNumber()).isEqualTo("+61123456789");
        verify(contactRepository, times(1)).save(contact);
    }

    @Test
    void createContactWithDifferentPhoneNumbers() {
        ContactDto contactDto2 = new ContactDto();
        contactDto2.setId(2L);
        contactDto2.setName("Jane Doe");
        contactDto2.setPhoneNumber("+61987654321");

        Contact contact2 = new Contact();
        contact2.setId(2L);
        contact2.setName("Jane Doe");
        contact2.setPhoneNumber("+61987654321");

        when(contactMapper.toContactEntity(contactDto2)).thenReturn(contact2);
        when(contactRepository.save(contact2)).thenReturn(contact2);
        when(contactMapper.toContactDto(contact2)).thenReturn(contactDto2);

        ContactDto result = contactService.create(contactDto2);

        assertThat(result.getPhoneNumber()).isEqualTo("+61987654321");
    }

    @Test
    void getContactByIdSuccessfully() {
        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

        Contact result = contactService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
    }

    @Test
    void getContactByIdNotFound() {
        when(contactRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contactService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Contact not found");
    }

    @Test
    void getAllContactsReturnsMultipleContacts() {
        Contact contact2 = new Contact();
        contact2.setId(2L);
        contact2.setName("Jane Doe");
        contact2.setPhoneNumber("+61987654321");

        ContactDto contactDto2 = new ContactDto();
        contactDto2.setId(2L);
        contactDto2.setName("Jane Doe");
        contactDto2.setPhoneNumber("+61987654321");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Contact> contactPage = new PageImpl<>(Arrays.asList(contact, contact2), pageable, 2);

        when(contactRepository.findAll(pageable)).thenReturn(contactPage);
        when(contactMapper.toContactDto(contact)).thenReturn(contactDto);
        when(contactMapper.toContactDto(contact2)).thenReturn(contactDto2);

        Page<ContactDto> result = contactService.getAllContacts(0, 10);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getName()).isEqualTo("John Doe");
        assertThat(result.getContent().get(1).getName()).isEqualTo("Jane Doe");
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void getAllContactsReturnsEmptyList() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contact> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(contactRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<ContactDto> result = contactService.getAllContacts(0, 10);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    void deleteContactSuccessfully() {
        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

        contactService.delete(1L);

        verify(contactRepository, times(1)).findById(1L);
        verify(contactRepository, times(1)).delete(contact);
    }

    @Test
    void deleteNonExistentContact() {
        when(contactRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contactService.delete(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getOrCreateContactWithExistingId() {
        when(contactRepository.findById(1L)).thenReturn(Optional.of(contact));

        Contact result = contactService.getOrCreateContact(contact);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(contactRepository, never()).save(any());
    }

    @Test
    void getOrCreateContactByPhoneNumberWhenExists() {
        Contact contactWithoutId = new Contact();
        contactWithoutId.setName("John Doe");
        contactWithoutId.setPhoneNumber("+61123456789");

        when(contactRepository.findByPhoneNumber("+61123456789")).thenReturn(Optional.of(contact));

        Contact result = contactService.getOrCreateContact(contactWithoutId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(contactRepository, never()).save(any());
    }

    @Test
    void getOrCreateContactCreatesNewWhenNotExists() {
        Contact newContact = new Contact();
        newContact.setName("New Contact");
        newContact.setPhoneNumber("+61555555555");

        Contact savedContact = new Contact();
        savedContact.setId(3L);
        savedContact.setName("New Contact");
        savedContact.setPhoneNumber("+61555555555");

        when(contactRepository.findByPhoneNumber("+61555555555")).thenReturn(Optional.empty());
        when(contactRepository.save(newContact)).thenReturn(savedContact);

        Contact result = contactService.getOrCreateContact(newContact);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        verify(contactRepository, times(1)).save(newContact);
    }

    @Test
    void getOrCreateContactWithNullId() {
        Contact contactWithoutId = new Contact();
        contactWithoutId.setName("John Doe");
        contactWithoutId.setPhoneNumber("+61123456789");

        when(contactRepository.findByPhoneNumber("+61123456789")).thenReturn(Optional.of(contact));

        Contact result = contactService.getOrCreateContact(contactWithoutId);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getOrCreateMultipleNewContacts() {
        Contact contact1 = new Contact();
        contact1.setName("Contact1");
        contact1.setPhoneNumber("+61111111111");

        Contact contact2 = new Contact();
        contact2.setName("Contact2");
        contact2.setPhoneNumber("+61222222222");

        Contact savedContact1 = new Contact();
        savedContact1.setId(1L);
        savedContact1.setName("Contact1");
        savedContact1.setPhoneNumber("+61111111111");

        Contact savedContact2 = new Contact();
        savedContact2.setId(2L);
        savedContact2.setName("Contact2");
        savedContact2.setPhoneNumber("+61222222222");

        when(contactRepository.findByPhoneNumber("+61111111111")).thenReturn(Optional.empty());
        when(contactRepository.save(contact1)).thenReturn(savedContact1);
        when(contactRepository.findByPhoneNumber("+61222222222")).thenReturn(Optional.empty());
        when(contactRepository.save(contact2)).thenReturn(savedContact2);

        Contact result1 = contactService.getOrCreateContact(contact1);
        Contact result2 = contactService.getOrCreateContact(contact2);

        assertThat(result1.getId()).isEqualTo(1L);
        assertThat(result2.getId()).isEqualTo(2L);
        verify(contactRepository, times(2)).save(any());
    }

    @Test
    void getAllContactsWithLargeDataset() {
        List<Contact> contacts = new ArrayList<>();
        List<ContactDto> contactDtos = new ArrayList<>();

        for (int i = 1; i <= 100; i++) {
            Contact c = new Contact();
            c.setId((long) i);
            c.setName("Contact" + i);
            c.setPhoneNumber("+611" + String.format("%08d", i));
            contacts.add(c);

            ContactDto cd = new ContactDto();
            cd.setId((long) i);
            cd.setName("Contact" + i);
            cd.setPhoneNumber("+611" + String.format("%08d", i));
            contactDtos.add(cd);
        }

        Pageable pageable = PageRequest.of(0, 20);
        List<Contact> pageContacts = contacts.subList(0, 20);
        Page<Contact> contactPage = new PageImpl<>(pageContacts, pageable, 100);

        when(contactRepository.findAll(pageable)).thenReturn(contactPage);
        for (int i = 0; i < 20; i++) {
            when(contactMapper.toContactDto(pageContacts.get(i))).thenReturn(contactDtos.get(i));
        }

        Page<ContactDto> result = contactService.getAllContacts(0, 20);

        assertThat(result.getContent()).hasSize(20);
        assertThat(result.getTotalElements()).isEqualTo(100);
        assertThat(result.getTotalPages()).isEqualTo(5);
    }
}
