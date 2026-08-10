package com.reece.addressbook.repository;

import com.reece.addressbook.model.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    /**
     * Used to avoid creating duplicate contacts.
     */
    Optional<Contact> findByPhoneNumber(Long phoneNumber);
}
