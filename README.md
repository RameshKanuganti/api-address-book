# Branch Manager Address Book API

A RESTful Web Service built with Spring Boot, Spring Data JPA, and H2 database to manage branch address books and contacts as per the Address Book Tech Task requirements.

---

## 📋 Overview & Requirements

The system allows branch managers to manage contacts organized into address books. Key business requirements satisfied by this service:

1. **Multiple Address Books**: Support creation and maintenance of multiple address books per branch manager/type (e.g. `CIVIL`, `DOMESTIC`, `FINANCIAL`, `INDUSTRIAL`, `INFRASTRUCTURE`, `IRRIGATION`).
2. **Contact Management**: Ability to add contacts to address books and remove contacts from address books.
3. **Contact Sharing & Deduplication**:
   - Contacts are uniquely identified by their phone number (`phoneNumber`).
   - The same contact can belong to multiple address books without duplicate database entries.
   - Removing a contact from an address book unlinks it; if no other address book references that contact, it is automatically cleaned up from the database.
4. **Unique Contacts Listing**: Retrieve a consolidated, unique list of contacts across selected address books or all address books.
5. **Global Contact Listing**: Retrieve all contacts across all address books with pagination support.

---

## 🛠️ Tech Stack

- **Java Version**: 17
- **Framework**: Spring Boot 4.0.7
- **Persistence**: Spring Data JPA / Hibernate
- **Database**: In-Memory H2 Database
- **API Documentation**: SpringDoc OpenAPI / Swagger UI
- **Build Tool**: Apache Maven

---

## 🚀 Getting Started

### Prerequisites
- Java Development Kit (JDK) 17 or higher
- Apache Maven 3.8+ (or use the included `./mvnw` / `mvnw.cmd` wrapper)

### Build the Project
```bash
# Clean and run all unit and integration tests
./mvnw clean install
```

### Run the Application
```bash
./mvnw spring-boot:run
```
The server will start on `http://localhost:8080`.

---

## 📖 API Documentation & Endpoints

Base URL: `/api/v1`

### 📚 Address Book Management

#### 1. Create Address Book
* **Endpoint**: `POST /api/v1/address-books`
* **Description**: Creates a new address book with branch manager name, type, and optional initial contacts.
* **Request Body**:
```json
{
  "branchManager": "Nitesh",
  "type": "DOMESTIC",
  "contacts": [
    {
      "name": "Ramesh",
      "phoneNumber": 123456789
    }
  ]
}
```
* **Response**: `201 Created` with created `AddressBookDto`.

---

#### 2. Get All Address Books
* **Endpoint**: `GET /api/v1/address-books`
* **Description**: Fetches all existing address books with their associated contacts.
* **Response**: `200 OK` with `List<AddressBookDto>`.

---

#### 3. Get Address Book by ID
* **Endpoint**: `GET /api/v1/address-books/{id}`
* **Description**: Fetches details of a specific address book by its unique ID.
* **Path Parameter**: `id` (Long) - Address Book ID.
* **Response**: `200 OK` with `AddressBookDto` or `404 Not Found`.

---

#### 4. Delete Address Book
* **Endpoint**: `DELETE /api/v1/address-books/{id}`
* **Description**: Deletes an address book by ID. Unlinks its contacts (and cleans up unreferenced contacts from DB).
* **Path Parameter**: `id` (Long) - Address Book ID.
* **Response**: `204 No Content`.

---

### 👤 Contact Management within Address Books

#### 5. Add Contact to Address Book
* **Endpoint**: `POST /api/v1/address-books/{addressBookId}/contacts`
* **Description**: Creates a new contact (or reuses an existing contact with the same phone number) and adds it to the specified address book.
* **Path Parameter**: `addressBookId` (Long) - Target Address Book ID.
* **Request Body**:
```json
{
  "name": "Andrew",
  "phoneNumber": 234567890
}
```
* **Response**: `201 Created` with updated `AddressBookDto`.

---

#### 6. Remove Contact from Address Book
* **Endpoint**: `DELETE /api/v1/address-books/{addressBookId}/contacts/{contactId}`
* **Description**: Removes a contact from the specified address book. If the contact is no longer referenced in any other address book, it is automatically removed from the database.
* **Path Parameters**:
  - `addressBookId` (Long) - Address Book ID.
  - `contactId` (Long) - Contact ID to remove.
* **Response**: `200 OK` with updated `AddressBookDto`.

---

### 🔍 Cross-Address Book Contact Queries

#### 7. Get Unique Contacts Across Address Books
* **Endpoint**: `GET /api/v1/address-books/contacts/unique`
* **Description**: Returns a consolidated set of unique contacts. If `addressBookIds` query parameter is provided, returns unique contacts across only those specified address books.
* **Query Parameter**: `addressBookIds` (optional, comma-separated List of Longs, e.g., `?addressBookIds=1,2`)
* **Response**: `200 OK` with `Set<ContactDto>`.

---

#### 8. Get All Contacts with Pagination
* **Endpoint**: `GET /api/v1/address-books/contacts/{pageNo}/{pageSize}`
* **Description**: Returns a paginated list of all contacts stored in the system.
* **Path Parameters**:
  - `pageNo` (int) - Zero-based page index (e.g. `0`).
  - `pageSize` (int) - Number of items per page (e.g. `10`).
* **Response**: `200 OK` with Spring Data `Page<ContactDto>`.

---

## 🛠️ Additional Resources

- **Swagger UI**: Interactive API documentation available at:
  - `http://localhost:8080/swagger-ui.html` or `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI Spec**: `http://localhost:8080/v3/api-docs`
- **H2 Web Console**: Access in-memory database interface at:
  - `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: *(leave empty)*

