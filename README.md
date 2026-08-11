# Branch Manager Address Book API

A RESTful API for branch managers to manage address books and contacts.

The application supports multiple address books, shared contacts, contact deduplication by phone number, contact removal, unique contact queries across address books, and paginated contact retrieval.

---

## Functional Overview

The Address Book API provides the following functionality:

* Create an address book.
* Retrieve address books.
* Retrieve an address book by ID.
* Delete an address book.
* Add contacts to an address book.
* Remove contacts from an address book.
* Reuse an existing contact when the same phone number is provided.
* Allow a contact to belong to multiple address books.
* Remove an unreferenced contact when it is no longer associated with any address book.
* Retrieve unique contacts across address books.
* Retrieve unique contacts for selected address books.
* Retrieve contacts using pagination.
* Validate supported address book types.
* Provide sample address books, contacts, and relationships for demonstration.
* Expose interactive API documentation through Swagger/OpenAPI.

---

## Address Book Types

The application currently supports the following address book types:

```text
DOMESTIC
CIVIL
INDUSTRIAL
FINANCIAL
INFRASTRUCTURE
IRRIGATION
```

---

# Address Book APIs

Base URL:

```text
http://localhost:8080/api/v1
```

## Create Address Book

Creates a new address book.

```http
POST /api/v1/address-books
```

### Request

```json
{
  "branchManager": "Nitesh",
  "type": "DOMESTIC",
  "contacts": [
    {
      "name": "Andrew",
      "phoneNumber": "+61234567890"
    }
  ]
}
```

### Response

```text
201 Created
```

The address book is created with the supplied branch manager, address book type, and contacts.

---

## Get All Address Books

Retrieves all address books.

```http
GET /api/v1/address-books
```

### Response

```text
200 OK
```

The response contains the address books and their associated contacts.

---

## Get Address Book by ID

Retrieves a specific address book.

```http
GET /api/v1/address-books/{id}
```

Example:

```http
GET /api/v1/address-books/1
```

### Responses

```text
200 OK
404 Not Found
```

---

## Delete Address Book

Deletes an address book.

```http
DELETE /api/v1/address-books/{id}
```

Example:

```http
DELETE /api/v1/address-books/1
```

When an address book is deleted, its contact relationships are removed.

If a contact is no longer associated with any address book, the contact can also be removed.

---

# Contact APIs

## Add Contact to Address Book

Adds a contact to an existing address book.

```http
POST /api/v1/address-books/{addressBookId}/contacts
```

Example:

```http
POST /api/v1/address-books/1/contacts
```

### Request

```json
{
  "name": "Andrew",
  "phoneNumber": "+61234567890"
}
```

### Contact Reuse

Contacts are identified by their phone number.

If the supplied phone number already exists, the existing contact is reused instead of creating another contact record.

This allows the same contact to be associated with multiple address books.

For example:

```text
Address Book 1 ─────┐
                    │
                    ▼
                Contact
                    ▲
                    │
Address Book 2 ─────┘
```

---

## Remove Contact from Address Book

Removes a contact from a specific address book.

```http
DELETE /api/v1/address-books/{addressBookId}/contacts/{contactId}
```

Example:

```http
DELETE /api/v1/address-books/1/contacts/2
```

The contact is removed from the selected address book.

If the contact is not associated with any other address book, it is no longer required and can be removed from the database.

---

# Contact Query APIs

## Get Unique Contacts

Retrieves unique contacts across all address books.

```http
GET /api/v1/address-books/contacts/unique
```

### Response

```text
200 OK
```

The result contains each contact only once, even when the contact belongs to multiple address books.

---

## Get Unique Contacts for Selected Address Books

Retrieves unique contacts from selected address books.

```http
GET /api/v1/address-books/contacts/unique?addressBookIds=1,2
```

The `addressBookIds` parameter specifies the address books that should be included in the query.

For example:

```text
Address Book 1 -> Contact A
Address Book 2 -> Contact A
Address Book 3 -> Contact B
```

Requesting address books `1,2` returns:

```text
Contact A
```

only once.

---

## Get Contacts with Pagination

Retrieves contacts using page number and page size.

```http
GET /api/v1/address-books/contacts?page=0&size=4
```

Example:

```http
GET /api/v1/address-books/contacts?page=0&size=10
```

### Parameters

| Parameter  | Description                          |
| ---------- | ------------------------------------ |
| `pageNo`   | Zero-based page number               |
| `pageSize` | Number of contacts returned per page |

Example:

```text
pageNo = 0
pageSize = 10
```

This returns the first page containing up to 10 contacts.

---

# Sample Data

The application includes sample database schema and data:

```text
src/main/resources/schema.sql
src/main/resources/data.sql
```

The sample dataset contains:

* 10 contacts
* 5 address books
* Multiple address-book/contact relationships
* Contacts shared across multiple address books

### Sample Address Books

| Branch Manager | Type       |
| -------------- | ---------- |
| Nitesh         | DOMESTIC   |
| Max            | IRRIGATION |
| Jack           | INDUSTRIAL |
| Harry          | INDUSTRIAL |
| Steve          | IRRIGATION |

The sample data is useful for testing the API functionality through Swagger, Postman, curl, or another REST client.

---

# Functional Examples

## Example 1 — Create an Address Book

```http
POST /api/v1/address-books
```

```json
{
  "branchManager": "John",
  "type": "CIVIL",
  "contacts": [
    {
      "name": "Alice",
      "phoneNumber": "+61400000001"
    }
  ]
}
```

---

## Example 2 — Add the Same Contact to Another Address Book

Create or use another address book and add:

```json
{
  "name": "Alice",
  "phoneNumber": "+61400000001"
}
```

Because the phone number already exists, the existing contact is reused.

The resulting relationship is:

```text
Address Book A ──┐
                 ├── Alice (+61400000001)
Address Book B ──┘
```

No duplicate contact needs to be created.

---

## Example 3 — Retrieve Unique Contacts

```http
GET /api/v1/address-books/contacts/unique
```

If Alice belongs to three address books, Alice appears only once in the result.

---

## Example 4 — Retrieve Unique Contacts from Specific Address Books

```http
GET /api/v1/address-books/contacts/unique?addressBookIds=1,2
```

Only contacts associated with address books `1` and `2` are considered, and duplicate contacts are returned only once.

---

# Swagger / OpenAPI

Interactive API documentation is available when the application is running:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

Swagger UI can be used to execute the available API operations without requiring an external REST client.

---

# H2 Database Console

The H2 console is available for local database inspection:

```text
http://localhost:8080/h2-console
```

Default JDBC URL:

```text
jdbc:h2:mem:testdb
```

Username:

```text
sa
```

Password:

```text
test
```

The console can be used to inspect the address book, contact, and relationship data created by the sample dataset and API operations.

---

# Docker

The application can also be run as a Docker container.

Build the image:

```bash
docker build -t addressbook-api .
```

Run the container:

```bash
docker run --rm -p 8080:8080 addressbook-api
```

The API is then available at:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

H2 console:

```text
http://localhost:8080/h2-console
```

---

# Functional Test Scenarios

The application covers the following key scenarios:

| Scenario                               | Expected Behaviour                                      |
| -------------------------------------- | ------------------------------------------------------- |
| Create address book                    | Address book is created                                 |
| Retrieve address books                 | All available address books are returned                |
| Retrieve address book by ID            | Requested address book is returned                      |
| Address book does not exist            | `404 Not Found`                                         |
| Add new contact                        | Contact is created and associated with the address book |
| Add existing contact                   | Existing contact is reused                              |
| Same contact in multiple address books | One contact can have multiple address-book associations |
| Remove contact                         | Contact is removed from the selected address book       |
| Remove final contact association       | Unreferenced contact can be removed                     |
| Retrieve unique contacts               | Duplicate contacts are returned only once               |
| Filter unique contacts                 | Results are limited to selected address books           |
| Paginate contacts                      | Contacts are returned according to page and page size   |
| Invalid address book type              | Request validation fails                                |
| Delete address book                    | Address book and its relationships are removed          |
