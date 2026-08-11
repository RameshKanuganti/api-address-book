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
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1")
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
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addressBookService.createAddressBook(request));
    }

    /**
     * Returns all address books with contacts.
     * Example: GET /api/v1/address-books
     */
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

    /**
     * Adds a contact to an address book.
     * Returns the created/associated ContactDto rather than the full AddressBookDto
     * to follow REST sub-resource conventions.
     */
    @PostMapping("/address-books/{addressBookId}/contacts")
    public ResponseEntity<ContactDto> addNewContactToAddressBook(
            @PathVariable Long addressBookId,
            @Valid @RequestBody ContactDto contactRequest) {
        log.debug("Adding contact to address book id={}, name={}",
                addressBookId, contactRequest.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addressBookService.addNewContactToAddressBook(addressBookId, contactRequest));
    }

    @DeleteMapping("/address-books/{addressBookId}/contacts/{contactId}")
    public ResponseEntity<AddressBookDto> removeContactFromAddressBook(
            @PathVariable Long addressBookId,
            @PathVariable Long contactId) {
        return ResponseEntity.ok(
                addressBookService.removeContactFromAddressBook(addressBookId, contactId));
    }

    /**
     * Returns unique contacts for the supplied address book IDs.
     * Address book IDs must be provided via the {@code addressBookIds} query param.
     * Example: GET /api/v1/address-books/contacts/unique?addressBookIds=1,2,3
     * All IDs must exist; any missing ID results in 404.
     */
    @GetMapping("/address-books/contacts/unique")
    public ResponseEntity<Set<ContactDto>> getUniqueContactsAcrossAddressBooks(
            @RequestParam(required = false) List<Long> addressBookIds) {

        if (ObjectUtils.isEmpty(addressBookIds)) {
            throw new BusinessValidationException(
                    "Address book IDs parameter is required and cannot be empty");
        }
        return ResponseEntity.ok(
                addressBookService.getUniqueContactsAcrossAddressBooks(addressBookIds));
    }

    /**
     * Paginated list of all contacts across all address books.
     * Uses standard page/size query parameters instead of path variables.
     * Example: GET /api/v1/contacts?page=0&size=20
     */
    @GetMapping("/contacts")
    public ResponseEntity<Page<ContactDto>> getAllContacts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size) {
        return ResponseEntity.ok(contactService.getAllContacts(page, size));
    }
}
