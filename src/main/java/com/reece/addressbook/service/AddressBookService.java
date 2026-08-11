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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;
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

    @Transactional
    public AddressBookDto createAddressBook(AddressBookDto dto) {
        AddressBook addressBook = addressBookMapper.toAddressBookEntity(dto);

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
     * Adds a contact to an existing address book and returns the associated contact.
     * Returning the ContactDto (rather than the full AddressBookDto) follows REST
     * conventions for a POST to a sub-resource.
     */
    @Transactional
    public ContactDto addNewContactToAddressBook(Long addressBookId, ContactDto contactDto) {
        AddressBook addressBook = getAddressBookEntity(addressBookId);
        Contact contact = contactMapper.toContactEntity(contactDto);
        Contact persistedContact = contactService.getOrCreateContact(contact);

        addressBook.addContact(persistedContact);
        AddressBook addressBookEntity = addressBookRepository.save(addressBook);
        log.info("Added new contact id={}, name={} to address book id={}",
                persistedContact.getId(), persistedContact.getName(), addressBookId);
        return contactMapper.toContactDto(persistedContact);
    }

    @Transactional(readOnly = true)
    public List<AddressBookDto> getAllAddressBooks() {
        return addressBookRepository.findAll().stream()
                .map(addressBookMapper::toAddressBookDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AddressBookDto getAddressBookById(Long id) {
        return addressBookMapper.toAddressBookDto(getAddressBookEntity(id));
    }

    @Transactional
    public void deleteAddressBook(Long addressBookId) {
        if (!addressBookRepository.existsById(addressBookId)) {
            throw new ResourceNotFoundException("Address book not found: " + addressBookId);
        }
        List<Long> contactIds = addressBookRepository.findContactIdsByAddressBookId(addressBookId);
        addressBookRepository.deleteById(addressBookId);
        if (!contactIds.isEmpty()) {
            int deletedContacts = contactRepository.deleteOrphanedContacts(contactIds);
            log.info("Deleted address book id={}; removed {} orphaned contacts", addressBookId, deletedContacts);
        }
    }

    /**
     * Removes a contact from an address book.
     * Validates that the contact actually belongs to the specified address book
     * before attempting removal — avoids silent no-ops and misleading responses.
     * If the contact is no longer referenced by any address book it is deleted.
     */

    @Transactional
    public AddressBookDto removeContactFromAddressBook(
            Long addressBookId,
            Long contactId) {

        getAddressBookEntity(addressBookId);
        contactService.getById(contactId);

        if (!addressBookRepository.existsContactInAddressBook(
                addressBookId, contactId)) {

            throw new BusinessValidationException(
                    String.format(
                            "Contact id=%d does not belong to address book id=%d",
                            contactId,
                            addressBookId));
        }

        int removed = addressBookRepository.removeContactFromAddressBook(
                addressBookId,
                contactId);

        if (removed != 1) {
            throw new IllegalStateException(
                    String.format(
                            "Failed to remove contact id=%d from address book id=%d",
                            contactId,
                            addressBookId));
        }

        long remainingReferences =
                addressBookRepository.countByContactId(contactId);

        if (remainingReferences == 0) {
            contactRepository.deleteById(contactId);

            log.info(
                    "Removed orphaned contact id={} after removing it from address book id={}",
                    contactId,
                    addressBookId);
        } else {
            log.info(
                    "Removed contact id={} from address book id={}; remaining references={}",
                    contactId,
                    addressBookId,
                    remainingReferences);
        }

        return getAddressBookById(addressBookId);
    }

    /**
     * Unique contacts for the given address book IDs via a DB-level DISTINCT query.
     * All requested IDs must exist; missing IDs result in a 404 to make the
     * API contract explicit rather than silently returning partial results.
     */
    @Transactional(readOnly = true)
    public Set<ContactDto> getUniqueContactsAcrossAddressBooks(List<Long> addressBookIds) {
        List<Long> foundIds = addressBookRepository.findAllById(addressBookIds)
                .stream().map(AddressBook::getId).collect(Collectors.toList());
        List<Long> missingIds = addressBookIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toList());
        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException("Address books not found for ids: " + missingIds);
        }

        return addressBookRepository.findDistinctContactsByAddressBookIds(addressBookIds).stream()
                .map(contactMapper::toContactDto)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private AddressBook getAddressBookEntity(Long id) {
        return addressBookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AddressBook not found, id=" + id));
    }
}
