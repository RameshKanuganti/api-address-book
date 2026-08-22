package com.reece.addressbook.repository;

import com.reece.addressbook.model.AddressBook;
import com.reece.addressbook.model.AddressBookType;
import com.reece.addressbook.model.Contact;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RepositoryQueryTest {

    @Autowired
    private AddressBookRepository addressBookRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void customQueriesWorkAcrossAddressBooksAndContacts() {
        Contact shared = new Contact();
        shared.setName("Shared");
        shared.setPhoneNumber("+61111111111");
        entityManager.persist(shared);

        Contact exclusive = new Contact();
        exclusive.setName("Exclusive");
        exclusive.setPhoneNumber("+62222222222");
        entityManager.persist(exclusive);

        Contact orphan = new Contact();
        orphan.setName("Orphan");
        orphan.setPhoneNumber("+63333333333");
        entityManager.persist(orphan);

        entityManager.flush();

        AddressBook first = new AddressBook();
        first.setBranchManager("First");
        first.setType(AddressBookType.DOMESTIC);
        first.setContacts(new HashSet<>(Set.of(shared, exclusive)));
        entityManager.persist(first);

        AddressBook second = new AddressBook();
        second.setBranchManager("Second");
        second.setType(AddressBookType.INDUSTRIAL);
        second.setContacts(new HashSet<>(Set.of(shared)));
        entityManager.persist(second);

        entityManager.flush();

        assertThat(addressBookRepository.countByContactId(shared.getId())).isEqualTo(2);
        assertThat(addressBookRepository.countByContactId(exclusive.getId())).isEqualTo(1);
        assertThat(addressBookRepository.existsContactInAddressBook(first.getId(), shared.getId())).isTrue();
        assertThat(addressBookRepository.existsContactInAddressBook(second.getId(), exclusive.getId())).isFalse();
        assertThat(addressBookRepository.findContactIdsByAddressBookId(first.getId()))
                .containsExactlyInAnyOrder(shared.getId(), exclusive.getId());
        assertThat(addressBookRepository.findDistinctContactsByAddressBookIds(List.of(first.getId(), second.getId())))
                .extracting(Contact::getId)
                .containsExactlyInAnyOrder(shared.getId(), exclusive.getId());
        assertThat(addressBookRepository.findAllDistinctContacts())
                .extracting(Contact::getId)
                .contains(shared.getId(), exclusive.getId());

        assertThat(addressBookRepository.removeContactFromAddressBook(first.getId(), exclusive.getId())).isEqualTo(1);
        assertThat(addressBookRepository.existsContactInAddressBook(first.getId(), exclusive.getId())).isFalse();
        assertThat(addressBookRepository.countByContactId(exclusive.getId())).isZero();

        int deleted = contactRepository.deleteOrphanedContacts(List.of(exclusive.getId(), orphan.getId()));
        assertThat(deleted).isEqualTo(2);
        entityManager.clear();
        assertThat(contactRepository.findById(exclusive.getId())).isEmpty();
        assertThat(contactRepository.findById(orphan.getId())).isEmpty();
        assertThat(contactRepository.findById(shared.getId())).isPresent();
    }
}

