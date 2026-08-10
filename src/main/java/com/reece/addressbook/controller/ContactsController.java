/*
package com.reece.addressbook.controller;

import com.reece.addressbook.dto.ContactDto;
import com.reece.addressbook.service.AddressBookService;
import com.reece.addressbook.service.ContactService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/")
public class ContactsController {

    private static final Logger log = LoggerFactory.getLogger(AddressBookController.class);

    private final AddressBookService addressBookService;
    private final ContactService contactService;


    public ContactsController(AddressBookService addressBookService,
                              ContactService contactService) {
        this.addressBookService = addressBookService;
        this.contactService = contactService;
    }

    @PostMapping("/contacts")
    public ResponseEntity<ContactDto> createContact(
            @Valid @RequestBody ContactDto request) {

        ContactDto response = contactService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/contacts")
    public ResponseEntity<List<ContactDto>> getAllContacts() {
        return ResponseEntity.ok(contactService.getAll());
    }

    @DeleteMapping("/contacts/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        contactService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
*/
