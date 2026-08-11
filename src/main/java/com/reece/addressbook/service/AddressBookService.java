package com.reece.addressbook.service;

import com.reece.addressbook.dto.AddressBookDto;
import com.reece.addressbook.dto.ContactDto;
import com.reece.addressbook.exception.BusinessValidationException;
import com.reece.addressbook.exception.ResourceNotFoundException;
import com.reece.addressbook.mapper.AddressBookMapper;
import com.reece.addressbook.mapper.ContactMapper;
import com.reece.addressbook.model.AddressBook;
import com.reece.addressbook.model.Contact;
import com.reece.addressbook.repository.AddressBookRepository;
import com.reece.addressbook.repository.ContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
public class AddressBookService {

    private static final Logger log = LoggerFactory.getLogger(AddressBookService.class);

    private final AddressBookRepository addressBookRepository;
    private final ContactRepository contactRepository;
    private final ContactService contactService;
    private final AddressBookMapper addressBookMapper;
    private final ContactMapper contactMapper;

    public AddressBookService(AddressBookRepository addressBookRepository,
                              ContactRepository contactRepository,
                              ContactService contactService,
                              AddressBookMapper addressBookMapper,
                              ContactMapper contactMapper) {
        this.addressBookRepository = addressBookRepository;
        this.contactRepository = contactRepository;
        this.contactService = contactService;
        this.addressBookMapper = addressBookMapper;
        this.contactMapper = contactMapper;
    }

    public AddressBookDto createAddressBook(AddressBookDto dto) {
        AddressBook addressBook = addressBookMapper.toAddressBookEntity(dto);

        // Persist contacts first before saving the AddressBook
        if (!ObjectUtils.isEmpty(addressBook.getContacts())) {
            Set<Contact> persistedContacts = new HashSet<>();
            for (Contact contact : addressBook.getContacts()) {
                Contact persistedContact = contactService.getOrCreateContact(contact);
                persistedContacts.add(persistedContact);
            }
            addressBook.setContacts(persistedContacts);
        }

        AddressBook addressBookEntity = addressBookRepository.save(addressBook);
        log.info("Created address book id={}", addressBookEntity.getId());
        return addressBookMapper.toAddressBookDto(addressBookEntity);
    }

    /**
     * Creates or gets a contact and adds it to an existing AddressBook.
     * If the contact already exists (same name and phone number), it will be reused.
     * If it's new, it will be created and then added to the AddressBook.
     */
    public AddressBookDto addNewContactToAddressBook(Long addressBookId, ContactDto contactDto) {
        AddressBook addressBook = getAddressBookEntity(addressBookId);

        // Convert DTO to entity and get or create the contact
        Contact contact = contactMapper.toContactEntity(contactDto);
        Contact persistedContact = contactService.getOrCreateContact(contact);

        addressBook.addContact(persistedContact);
        AddressBook addressBookEntity = addressBookRepository.save(addressBook);
        log.info("Added new contact id={}, name={} to address book id={}",
                persistedContact.getId(), persistedContact.getName(), addressBookId);
        return addressBookMapper.toAddressBookDto(addressBookEntity);
    }

    public List<AddressBookDto> getAllAddressBooks() {
        return addressBookRepository.findAll().stream()
                .map(addressBookMapper::toAddressBookDto)
                .collect(Collectors.toList());
    }

    public AddressBookDto getAddressBookById(Long id) {
        return addressBookMapper.toAddressBookDto(getAddressBookEntity(id));
    }

    public void deleteAddressBook(Long id) {
        AddressBook addressBook = getAddressBookEntity(id);
        addressBookRepository.delete(addressBook);
        log.info("Deleted address book id={}", id);
    }

    public AddressBookDto removeContactFromAddressBook(Long addressBookId, Long contactId) {
        AddressBook addressBook = getAddressBookEntity(addressBookId);
        Contact contact = contactService.getById(contactId);

        addressBook.removeContact(contact);
        AddressBook addressBookEntity = addressBookRepository.save(addressBook);
        log.info("Removed contact id={} from address book id={}", contactId, addressBookId);
        
        // Check if this contact is used in any other address books
        long contactCount = addressBookRepository.countByContactId(contactId);
        if (contactCount == 0) {
            // Contact is not used in any address book, delete it from database
            contactRepository.delete(contact);
            log.info("Contact id={} was not used in any other address book, deleted from database", contactId);
        }
        
        return addressBookMapper.toAddressBookDto(addressBookEntity);
    }

    /**
     * Unique set of all contacts across all address books.
     */
    public Set<ContactDto> getUniqueContactsAcrossAllAddressBooks() {
        Set<Contact> uniqueContacts = addressBookRepository.findAll().stream()
                .flatMap(ab -> ab.getContacts().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return uniqueContacts.stream()
                .map(contactMapper::toContactDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Unique set of contacts across the provided address book ids. Throws BusinessValidationException
     * if the list is null or empty.
     */
    public Set<ContactDto> getUniqueContactsAcrossAddressBooks(List<Long> addressBookIds) {
        if (addressBookIds == null || addressBookIds.isEmpty()) {
            throw new BusinessValidationException("Address book IDs parameter is required and cannot be empty");
        }

        Set<Contact> uniqueContacts = addressBookRepository.findAllById(addressBookIds).stream()
                .flatMap(ab -> ab.getContacts().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return uniqueContacts.stream()
                .map(contactMapper::toContactDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private AddressBook getAddressBookEntity(Long id) {
        return addressBookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AddressBook not found, id=" + id));
    }
}
