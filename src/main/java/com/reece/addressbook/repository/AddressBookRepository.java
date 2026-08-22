package com.reece.addressbook.repository;

import com.reece.addressbook.model.AddressBook;
import com.reece.addressbook.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressBookRepository extends JpaRepository<AddressBook, Long> {

    /**
     * Count address books that reference a specific contact.
     * Used to decide whether to orphan-delete a contact after removing it.
     */
    @Query("SELECT COUNT(ab) FROM AddressBook ab JOIN ab.contacts c WHERE c.id = :contactId")
    long countByContactId(@Param("contactId") Long contactId);

    /**
     * Pushes the DISTINCT operation to the database instead of loading all
     * address books into memory and deduplicating in Java.
     * Avoids N+1 queries and heap pressure for large datasets.
     */
    @Query("SELECT DISTINCT c FROM AddressBook ab JOIN ab.contacts c WHERE ab.id IN :ids")
    List<Contact> findDistinctContactsByAddressBookIds(@Param("ids") List<Long> ids);

    /**
     * Returns every unique contact referenced by any address book.
     * DB-level DISTINCT avoids full table scan + Java-side deduplication.
     */
    @Query("SELECT DISTINCT c FROM AddressBook ab JOIN ab.contacts c")
    List<Contact> findAllDistinctContacts();

    @Query("""
            SELECT CASE WHEN COUNT(ab) > 0 THEN true ELSE false END
            FROM AddressBook ab
            JOIN ab.contacts c
            WHERE ab.id = :addressBookId
              AND c.id = :contactId
            """)
    boolean existsContactInAddressBook(
            @Param("addressBookId") Long addressBookId,
            @Param("contactId") Long contactId);

    @Modifying
    @Query(
            value = """
                    DELETE FROM addressbook_contact
                    WHERE addressbook_id = :addressBookId
                      AND contact_id = :contactId
                    """,
            nativeQuery = true
    )
    int removeContactFromAddressBook(
            @Param("addressBookId") Long addressBookId,
            @Param("contactId") Long contactId);

    @Query("""
            SELECT c.id
            FROM AddressBook ab
            JOIN ab.contacts c
            WHERE ab.id = :addressBookId
            """)
    List<Long> findContactIdsByAddressBookId(
            @Param("addressBookId") Long addressBookId);

    @Query("""
                select distinct ab
                from AddressBook ab
                left join fetch ab.contacts
            """)
    List<AddressBook> findAllWithContacts();

}
