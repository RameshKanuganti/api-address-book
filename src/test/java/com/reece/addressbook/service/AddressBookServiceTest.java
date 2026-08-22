package com.reece.addressbook.service;

import com.reece.addressbook.dto.AddressBookDto;
import com.reece.addressbook.dto.ContactDto;
import com.reece.addressbook.exception.BusinessValidationException;
import com.reece.addressbook.exception.ResourceNotFoundException;
import com.reece.addressbook.mapper.AddressBookMapper;
import com.reece.addressbook.mapper.ContactMapper;
import com.reece.addressbook.model.AddressBook;
import com.reece.addressbook.model.AddressBookType;
import com.reece.addressbook.model.Contact;
import com.reece.addressbook.repository.AddressBookRepository;
import com.reece.addressbook.repository.ContactRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressBookServiceTest {

    @Mock
    private AddressBookRepository addressBookRepository;
    @Mock
    private ContactRepository contactRepository;
    @Mock
    private ContactService contactService;
    @Mock
    private AddressBookMapper addressBookMapper;
    @Mock
    private ContactMapper contactMapper;

    @InjectMocks
    private AddressBookService addressBookService;

    private AddressBook addressBook;
    private AddressBookDto addressBookDto;
    private Contact contact;
    private ContactDto contactDto;

    @BeforeEach
    void setUp() {
        addressBook = new AddressBook();
        addressBook.setId(1L);
        addressBook.setBranchManager("Manager1");
        addressBook.setType(AddressBookType.DOMESTIC);

        addressBookDto = new AddressBookDto();
        addressBookDto.setId(1L);
        addressBookDto.setBranchManager("Manager1");
        addressBookDto.setType(AddressBookType.DOMESTIC);

        contact = new Contact();
        contact.setId(1L);
        contact.setName("John");
        contact.setPhoneNumber("+61123456789");

        contactDto = new ContactDto();
        contactDto.setId(1L);
        contactDto.setName("John");
        contactDto.setPhoneNumber("+61123456789");
    }

    // ── createAddressBook ─────────────────────────────────────────────────────

    @Test
    void createAddressBookWithContactsSuccessfully() {
        addressBookDto.setContacts(new HashSet<>(Arrays.asList(contactDto)));
        addressBook.setContacts(new HashSet<>(Arrays.asList(contact)));

        when(addressBookMapper.toAddressBookEntity(addressBookDto)).thenReturn(addressBook);
        when(contactService.getOrCreateContact(any(Contact.class))).thenReturn(contact);
        when(addressBookRepository.save(addressBook)).thenReturn(addressBook);
        when(addressBookMapper.toAddressBookDto(addressBook)).thenReturn(addressBookDto);

        AddressBookDto result = addressBookService.createAddressBook(addressBookDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getBranchManager()).isEqualTo("Manager1");
        verify(addressBookRepository, times(1)).save(addressBook);
    }

    @Test
    void createAddressBookWithoutContacts() {
        addressBookDto.setContacts(new HashSet<>());
        addressBook.setContacts(new HashSet<>());

        when(addressBookMapper.toAddressBookEntity(addressBookDto)).thenReturn(addressBook);
        when(addressBookRepository.save(addressBook)).thenReturn(addressBook);
        when(addressBookMapper.toAddressBookDto(addressBook)).thenReturn(addressBookDto);

        AddressBookDto result = addressBookService.createAddressBook(addressBookDto);

        assertThat(result).isNotNull();
        assertThat(result.getContacts()).isEmpty();
        verify(contactService, never()).getOrCreateContact(any());
    }

    @Test
    void createAddressBookWithMultipleContacts() {
        Contact contact2 = new Contact();
        contact2.setId(2L);
        contact2.setName("Jane");
        contact2.setPhoneNumber("+61987654321");

        ContactDto contactDto2 = new ContactDto();
        contactDto2.setId(2L);
        contactDto2.setName("Jane");
        contactDto2.setPhoneNumber("+61987654321");

        addressBookDto.setContacts(new HashSet<>(Arrays.asList(contactDto, contactDto2)));
        addressBook.setContacts(new HashSet<>(Arrays.asList(contact, contact2)));

        when(addressBookMapper.toAddressBookEntity(addressBookDto)).thenReturn(addressBook);
        when(contactService.getOrCreateContact(any(Contact.class))).thenReturn(contact, contact2);
        when(addressBookRepository.save(addressBook)).thenReturn(addressBook);
        when(addressBookMapper.toAddressBookDto(addressBook)).thenReturn(addressBookDto);

        AddressBookDto result = addressBookService.createAddressBook(addressBookDto);

        assertThat(result.getContacts()).hasSize(2);
        verify(contactService, times(2)).getOrCreateContact(any());
    }

    // ── addNewContactToAddressBook ────────────────────────────────────────────

    @Test
    void addNewContactToAddressBookSuccessfully() {
        when(addressBookRepository.findById(1L)).thenReturn(Optional.of(addressBook));
        when(contactMapper.toContactEntity(contactDto)).thenReturn(contact);
        when(contactService.getOrCreateContact(contact)).thenReturn(contact);
        when(addressBookRepository.save(addressBook)).thenReturn(addressBook);
        when(contactMapper.toContactDto(contact)).thenReturn(contactDto);

        ContactDto result = addressBookService.addNewContactToAddressBook(1L, contactDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(addressBookRepository, times(1)).findById(1L);
        verify(addressBookRepository, times(1)).save(addressBook);
    }

    @Test
    void addNewContactToNonExistentAddressBook() {
        when(addressBookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressBookService.addNewContactToAddressBook(999L, contactDto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── getAllAddressBooks ─────────────────────────────────────────────────────

    @Test
    void getAllAddressBooksReturnsMultipleBooks() {
        AddressBook addressBook2 = new AddressBook();
        addressBook2.setId(2L);
        addressBook2.setBranchManager("Manager2");

        List<AddressBook> books = Arrays.asList(addressBook, addressBook2);
        when(addressBookRepository.findAllWithContacts()).thenReturn(books);
        when(addressBookMapper.toAddressBookDto(addressBook)).thenReturn(addressBookDto);
        when(addressBookMapper.toAddressBookDto(addressBook2)).thenReturn(new AddressBookDto());

        List<AddressBookDto> result = addressBookService.getAllAddressBooks();

        assertThat(result).hasSize(2);
        verify(addressBookRepository, times(1)).findAllWithContacts();
    }

    @Test
    void getAllAddressBooksReturnsEmptyList() {
        when(addressBookRepository.findAllWithContacts()).thenReturn(new ArrayList<>());

        List<AddressBookDto> result = addressBookService.getAllAddressBooks();

        assertThat(result).isEmpty();
    }

    // ── getAddressBookById ─────────────────────────────────────────────────────

    @Test
    void getAddressBookByIdSuccessfully() {
        when(addressBookRepository.findById(1L)).thenReturn(Optional.of(addressBook));
        when(addressBookMapper.toAddressBookDto(addressBook)).thenReturn(addressBookDto);

        AddressBookDto result = addressBookService.getAddressBookById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getAddressBookByIdNotFound() {
        when(addressBookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressBookService.getAddressBookById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── deleteAddressBook ─────────────────────────────────────────────────────

    @Test
    void deleteAddressBookSuccessfully() {

        // Arrange
        Long addressBookId = 1L;
        List<Long> contactIds = List.of(1L);

        when(addressBookRepository.existsById(addressBookId)).thenReturn(true);
        when(addressBookRepository.findContactIdsByAddressBookId(addressBookId)).thenReturn(contactIds);
        when(contactRepository.deleteOrphanedContacts(contactIds)).thenReturn(1);

        // Act
        addressBookService.deleteAddressBook(addressBookId);

        // Assert
        verify(addressBookRepository).existsById(addressBookId);
        verify(addressBookRepository).findContactIdsByAddressBookId(addressBookId);
        verify(addressBookRepository).deleteById(addressBookId);
        verify(contactRepository).deleteOrphanedContacts(contactIds);
        verifyNoMoreInteractions(addressBookRepository, contactRepository);
    }


    @Test
    void deleteNonExistentAddressBook() {
        when(addressBookRepository.existsById(999L))
                .thenReturn(false);

        assertThatThrownBy(
                () -> addressBookService.deleteAddressBook(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(addressBookRepository)
                .existsById(999L);

        verify(addressBookRepository, never())
                .deleteById(anyLong());

        verify(contactRepository, never())
                .deleteOrphanedContacts(any());
    }


    // ── removeContactFromAddressBook ──────────────────────────────────────────

    @Test
    void removeContactFromAddressBookSuccessfully() {
        when(addressBookRepository.findById(1L))
                .thenReturn(Optional.of(addressBook));
        when(contactService.getById(1L))
                .thenReturn(contact);
        when(addressBookRepository.existsContactInAddressBook(1L, 1L))
                .thenReturn(true);
        when(addressBookRepository.removeContactFromAddressBook(1L, 1L))
                .thenReturn(1);
        when(addressBookRepository.countByContactId(1L))
                .thenReturn(0L);
        when(addressBookMapper.toAddressBookDto(addressBook))
                .thenReturn(addressBookDto);

        AddressBookDto result =
                addressBookService.removeContactFromAddressBook(1L, 1L);

        assertThat(result).isNotNull();

        verify(addressBookRepository)
                .existsContactInAddressBook(1L, 1L);

        verify(addressBookRepository)
                .removeContactFromAddressBook(1L, 1L);

        verify(addressBookRepository)
                .countByContactId(1L);

        verify(contactRepository)
                .deleteById(1L);
    }


    @Test
    void removeContactFromNonExistentAddressBook() {
        when(addressBookRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressBookService.removeContactFromAddressBook(999L, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeNonExistentContactFromAddressBook() {
        when(addressBookRepository.findById(1L)).thenReturn(Optional.of(addressBook));
        when(contactService.getById(999L)).thenThrow(new ResourceNotFoundException("Contact not found"));

        assertThatThrownBy(() -> addressBookService.removeContactFromAddressBook(1L, 999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeContactThatDoesNotBelongToAddressBook() {
        // Contact NOT added to addressBook
        when(addressBookRepository.findById(1L)).thenReturn(Optional.of(addressBook));
        when(contactService.getById(1L)).thenReturn(contact);

        assertThatThrownBy(() -> addressBookService.removeContactFromAddressBook(1L, 1L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("does not belong to address book");
    }

    @Test
    void removeContactUsedInMultipleAddressBooks() {
        when(addressBookRepository.findById(1L))
                .thenReturn(Optional.of(addressBook));
        when(contactService.getById(1L))
                .thenReturn(contact);
        when(addressBookRepository.existsContactInAddressBook(1L, 1L))
                .thenReturn(true);
        when(addressBookRepository.removeContactFromAddressBook(1L, 1L))
                .thenReturn(1);
        when(addressBookRepository.countByContactId(1L))
                .thenReturn(1L);
        when(addressBookMapper.toAddressBookDto(addressBook))
                .thenReturn(addressBookDto);

        AddressBookDto result =
                addressBookService.removeContactFromAddressBook(1L, 1L);

        assertThat(result).isNotNull();

        verify(addressBookRepository)
                .removeContactFromAddressBook(1L, 1L);

        verify(contactRepository, never())
                .deleteById(1L);
    }


    // ── getUniqueContacts ─────────────────────────────────────────────────────

    @Test
    void getUniqueContactsAcrossSpecificAddressBooks() {
        List<Long> addressBookIds = Arrays.asList(1L);

        when(addressBookRepository.findAllById(addressBookIds))
                .thenReturn(Arrays.asList(addressBook));
        when(addressBookRepository.findDistinctContactsByAddressBookIds(addressBookIds))
                .thenReturn(Arrays.asList(contact));
        when(contactMapper.toContactDto(contact)).thenReturn(contactDto);

        var result = addressBookService.getUniqueContactsAcrossAddressBooks(addressBookIds);

        assertThat(result).hasSize(1);
    }

    @Test
    void getUniqueContactsWithMissingAddressBookId() {
        List<Long> addressBookIds = Arrays.asList(1L, 999L);
        when(addressBookRepository.findAllById(addressBookIds))
                .thenReturn(Arrays.asList(addressBook)); // only 1L found

        assertThatThrownBy(() -> addressBookService.getUniqueContactsAcrossAddressBooks(addressBookIds))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void getUniqueContactsDeduplicatesViaDbQuery() {
        List<Long> addressBookIds = Arrays.asList(1L, 2L);
        AddressBook addressBook2 = new AddressBook();
        addressBook2.setId(2L);

        when(addressBookRepository.findAllById(addressBookIds))
                .thenReturn(Arrays.asList(addressBook, addressBook2));
        // DB-level DISTINCT returns contact only once
        when(addressBookRepository.findDistinctContactsByAddressBookIds(addressBookIds))
                .thenReturn(Arrays.asList(contact));
        when(contactMapper.toContactDto(contact)).thenReturn(contactDto);

        var result = addressBookService.getUniqueContactsAcrossAddressBooks(addressBookIds);

        assertThat(result).hasSize(1);
    }
}
