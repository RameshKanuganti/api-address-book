package com.reece.addressbook.repository;

import com.reece.addressbook.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    /**
     * Used to avoid creating duplicate contacts.
     * Phone number is stored in E.164 format (e.g. +61412345678).
     */
    Optional<Contact> findByPhoneNumber(String phoneNumber);

    @Query("""
            SELECT DISTINCT c
            FROM AddressBook ab
            JOIN ab.contacts c
            """)
    List<Contact> findDistinctContactsAcrossAddressBooks();

    @Modifying
    @Query("""
            DELETE FROM Contact c
            WHERE c.id IN :contactIds
              AND NOT EXISTS (
                  SELECT ab.id
                  FROM AddressBook ab
                  JOIN ab.contacts contact
                  WHERE contact.id = c.id
              )
            """)
    int deleteOrphanedContacts(
            @Param("contactIds") Collection<Long> contactIds);

}
