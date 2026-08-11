package com.reece.addressbook.service;

import com.reece.addressbook.dto.ContactDto;
import com.reece.addressbook.exception.ResourceNotFoundException;
import com.reece.addressbook.mapper.ContactMapper;
import com.reece.addressbook.model.Contact;
import com.reece.addressbook.repository.ContactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

@Service
public class ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactService.class);

    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;

    public ContactService(ContactRepository contactRepository, ContactMapper contactMapper) {
        this.contactRepository = contactRepository;
        this.contactMapper = contactMapper;
    }

    @Transactional
    public ContactDto create(ContactDto dto) {
        Contact contact = contactMapper.toContactEntity(dto);
        Contact saved = contactRepository.save(contact);
        log.info("Created contact with id={}", saved.getId());
        return contactMapper.toContactDto(saved);
    }

    @Transactional(readOnly = true)
    public Contact getById(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found, id=" + id));
    }

    @Transactional(readOnly = true)
    public Page<ContactDto> getAll(Pageable pageable) {
        return contactRepository.findAll(pageable)
                .map(contactMapper::toContactDto);
    }

    @Transactional(readOnly = true)
    public Page<ContactDto> getAllContacts(int pageNo, int pageSize) {
        return getAll(PageRequest.of(pageNo, pageSize));
    }

    @Transactional
    public void delete(Long id) {
        Contact contact = getById(id);
        contactRepository.delete(contact);
        log.info("Deleted contact id={}", id);
    }

    /**
     * Gets or creates a contact. If the contact has an ID, retrieves it from the database.
     * If no ID, checks if a contact with the same phone number exists.
     * If it exists, returns it; otherwise creates a new one.
     * This prevents duplicate contacts from being created.
     */
    @Transactional
    public Contact getOrCreateContact(Contact contact) {
        // If the contact has an ID, retrieve it from the database
        if (!ObjectUtils.isEmpty(contact.getId())) {
            return getById(contact.getId());
        }

        // Check if a contact with the same phone number already exists
        return contactRepository.findByPhoneNumber(contact.getPhoneNumber())
                .orElseGet(() -> {
                    // If not found, create a new contact
                    Contact contactEntity = contactRepository.save(contact);
                    log.info("Created contact with id={}, name={}, phoneNumber={}",
                            contactEntity.getId(), contactEntity.getName(), contactEntity.getPhoneNumber());
                    return contactEntity;
                });
    }
}
