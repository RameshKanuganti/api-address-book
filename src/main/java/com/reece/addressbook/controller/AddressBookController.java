package com.reece.addressbook.controller;

import com.reece.addressbook.dto.AddressBookDto;
import com.reece.addressbook.dto.ContactDto;
import com.reece.addressbook.exception.BusinessValidationException;
import com.reece.addressbook.service.AddressBookService;
import com.reece.addressbook.service.ContactService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/")
public class AddressBookController {

    private static final Logger log = LoggerFactory.getLogger(AddressBookController.class);

    private final AddressBookService addressBookService;
    private final ContactService contactService;

    public AddressBookController(AddressBookService addressBookService,
                                 ContactService contactService) {
        this.addressBookService = addressBookService;
        this.contactService = contactService;
    }

    @PostMapping("/address-books")
    public ResponseEntity<AddressBookDto> createAddressBook(
            @Valid @RequestBody AddressBookDto request) {

        log.debug("Creating address book: {}", request.getBranchManager());
        AddressBookDto response = addressBookService.createAddressBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/address-books/{addressBookId}/contacts")
    public ResponseEntity<AddressBookDto> addNewContactToAddressBook(
            @PathVariable Long addressBookId,
            @Valid @RequestBody ContactDto contactRequest) {

        log.debug("Adding new contact to address book id={}, contact name={}",
                addressBookId, contactRequest.getName());
        AddressBookDto response = addressBookService.addNewContactToAddressBook(addressBookId, contactRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/address-books")
    public ResponseEntity<List<AddressBookDto>> getAllAddressBooks() {
        return ResponseEntity.ok(addressBookService.getAllAddressBooks());
    }

    @GetMapping("/address-books/{id}")
    public ResponseEntity<AddressBookDto> getAddressBookById(@PathVariable Long id) {
        return ResponseEntity.ok(addressBookService.getAddressBookById(id));
    }

    @DeleteMapping("/address-books/{id}")
    public ResponseEntity<Void> deleteAddressBook(@PathVariable Long id) {
        addressBookService.deleteAddressBook(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/address-books/{addressBookId}/contacts/{contactId}")
    public ResponseEntity<AddressBookDto> removeContactFromAddressBook(
            @PathVariable Long addressBookId,
            @PathVariable Long contactId) {

        AddressBookDto response = addressBookService.removeContactFromAddressBook(addressBookId, contactId);
        return ResponseEntity.ok(response);
    }

    // ----- Unique contacts across address books -----

    /**
     * Get unique contacts across multiple address books.
     * `addressBookIds` must be provided as path variable or query parameter (e.g. /address-books/contacts/unique/1,2,3 or /address-books/contacts/unique?addressBookIds=1,2,3).
     */
    @GetMapping({"/address-books/contacts/unique/{addressBookIds}", "/address-books/contacts/unique"})
    public ResponseEntity<Set<ContactDto>> getUniqueContactsAcrossAddressBooks(
            @PathVariable(name = "addressBookIds", required = false) List<Long> pathAddressBookIds,
            @RequestParam(name = "addressBookIds", required = false) List<Long> queryAddressBookIds) {

        List<Long> addressBookIds = (pathAddressBookIds != null && !pathAddressBookIds.isEmpty())
                ? pathAddressBookIds
                : queryAddressBookIds;

        if (addressBookIds == null || addressBookIds.isEmpty()) {
            throw new BusinessValidationException("Address book IDs parameter is required and cannot be empty");
        }

        Set<ContactDto> contacts = addressBookService.getUniqueContactsAcrossAddressBooks(addressBookIds);
        return ResponseEntity.ok(contacts);
    }

    /**
     * Return all contacts (global) with pagination. This mirrors the existing ContactsController#getAllContacts
     * but is provided here as an alternative endpoint under address-books to fetch all contacts.
     * Contacts are unique by phoneNumber at the persistence/service layer.
     */
    @GetMapping("/address-books/contacts/{pageNo}/{pageSize}")
    public ResponseEntity<Page<ContactDto>> getAllContactsAcrossAddressBooks(@PathVariable int pageNo, @PathVariable int pageSize) {
        Page<ContactDto> contacts = contactService.getAllContacts(pageNo, pageSize);
        return ResponseEntity.ok(contacts);
    }
}
