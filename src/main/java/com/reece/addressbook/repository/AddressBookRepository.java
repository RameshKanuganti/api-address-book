package com.reece.addressbook.repository;

import com.reece.addressbook.model.AddressBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressBookRepository extends JpaRepository<AddressBook, Long> {
    
    /**
     * Count the number of address books that contain a specific contact.
     */
    @Query("SELECT COUNT(ab) FROM AddressBook ab JOIN ab.contacts c WHERE c.id = :contactId")
    long countByContactId(@Param("contactId") Long contactId);
}
