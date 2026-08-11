package com.reece.addressbook.controller;

import com.reece.addressbook.dto.AddressBookDto;
import com.reece.addressbook.dto.ContactDto;
import com.reece.addressbook.exception.BusinessValidationException;
import com.reece.addressbook.exception.ApiError;
import com.reece.addressbook.service.AddressBookService;
import com.reece.addressbook.service.ContactService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Address Book", description = "APIs for managing address books and contacts")
public class AddressBookController {

    private static final Logger log = LoggerFactory.getLogger(AddressBookController.class);

    private final AddressBookService addressBookService;
    private final ContactService contactService;

    public AddressBookController(AddressBookService addressBookService,
                                 ContactService contactService) {
        this.addressBookService = addressBookService;
        this.contactService = contactService;
    }

    @Operation(
            summary = "Create a new address book",
            description = "Creates an address book for a branch manager and returns the created address book resource."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Address book created",
                    content = @Content(schema = @Schema(implementation = AddressBookDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid address book request",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
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
    @Operation(
            summary = "List all address books",
            description = "Returns every address book in the system, including their associated contacts."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address books retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AddressBookDto.class)))),
            @ApiResponse(responseCode = "500", description = "Unexpected server error",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/address-books")
    public ResponseEntity<List<AddressBookDto>> getAllAddressBooks() {
        return ResponseEntity.ok(addressBookService.getAllAddressBooks());
    }

    @Operation(
            summary = "Get address book by ID",
            description = "Returns a single address book and its contacts for the supplied identifier."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Address book found",
                    content = @Content(schema = @Schema(implementation = AddressBookDto.class))),
            @ApiResponse(responseCode = "404", description = "Address book not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/address-books/{id}")
    public ResponseEntity<AddressBookDto> getAddressBookById(
            @Parameter(description = "Address book identifier", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(addressBookService.getAddressBookById(id));
    }

    @Operation(
            summary = "Delete address book",
            description = "Deletes the address book identified by the supplied ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Address book deleted"),
            @ApiResponse(responseCode = "404", description = "Address book not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/address-books/{id}")
    public ResponseEntity<Void> deleteAddressBook(
            @Parameter(description = "Address book identifier", example = "1")
            @PathVariable Long id) {
        addressBookService.deleteAddressBook(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Adds a contact to an address book.
     * Returns the created/associated ContactDto rather than the full AddressBookDto
     * to follow REST sub-resource conventions.
     */
    @Operation(
            summary = "Add a contact to an address book",
            description = "Creates a contact inside the selected address book and returns the created contact."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Contact created and added to the address book",
                    content = @Content(schema = @Schema(implementation = ContactDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid contact request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Address book not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/address-books/{addressBookId}/contacts")
    public ResponseEntity<ContactDto> addNewContactToAddressBook(
            @Parameter(description = "Address book identifier", example = "1")
            @PathVariable Long addressBookId,
            @Valid @RequestBody ContactDto contactRequest) {
        log.debug("Adding contact to address book id={}, name={}",
                addressBookId, contactRequest.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addressBookService.addNewContactToAddressBook(addressBookId, contactRequest));
    }

    @Operation(
            summary = "Remove a contact from an address book",
            description = "Removes the selected contact from the address book and returns the updated address book."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contact removed successfully",
                    content = @Content(schema = @Schema(implementation = AddressBookDto.class))),
            @ApiResponse(responseCode = "404", description = "Address book or contact not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/address-books/{addressBookId}/contacts/{contactId}")
    public ResponseEntity<AddressBookDto> removeContactFromAddressBook(
            @Parameter(description = "Address book identifier", example = "1")
            @PathVariable Long addressBookId,
            @Parameter(description = "Contact identifier", example = "10")
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
    @Operation(
            summary = "Get unique contacts across address books",
            description = "Returns a de-duplicated set of contacts across the requested address book IDs."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unique contacts retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ContactDto.class)))),
            @ApiResponse(responseCode = "400", description = "Missing or invalid addressBookIds parameter",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "One or more address books were not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/address-books/contacts/unique")
    public ResponseEntity<Set<ContactDto>> getUniqueContactsAcrossAddressBooks(
            @Parameter(description = "One or more address book IDs, for example: 1,2,3", example = "1,2,3")
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
    @Operation(
            summary = "List contacts with pagination",
            description = "Returns contacts across all address books using standard page and size query parameters."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contacts retrieved successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Invalid paging parameters",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/contacts")
    public ResponseEntity<Page<ContactDto>> getAllContacts(
            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size", example = "4")
            @RequestParam(defaultValue = "4") @Min(1) int size) {
        return ResponseEntity.ok(contactService.getAllContacts(page, size));
    }
}
