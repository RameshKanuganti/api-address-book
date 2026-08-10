package com.reece.addressbook.mapper;

import com.reece.addressbook.dto.AddressBookDto;
import com.reece.addressbook.model.AddressBook;
import com.reece.addressbook.model.Contact;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AddressBookMapper {

    private final ContactMapper contactMapper;

    public AddressBookMapper(ContactMapper contactMapper) {
        this.contactMapper = contactMapper;
    }

    public AddressBook toAddressBookEntity(AddressBookDto dto) {

        AddressBook addressBook = new AddressBook();
        addressBook.setId(dto.getId());
        addressBook.setBranchManager(dto.getBranchManager());
        addressBook.setType(dto.getType());
        
        if (!ObjectUtils.isEmpty(dto.getContacts())) {
            // Convert ContactDto objects to Contact entities
            Set<Contact> contacts = dto.getContacts().stream()
                    .map(contactMapper::toContactEntity)
                    .collect(Collectors.toCollection(HashSet::new));
            addressBook.setContacts(contacts);
        }
        return addressBook;
    }

    public AddressBookDto toAddressBookDto(AddressBook addressBook) {

        AddressBookDto addressBookDto = new AddressBookDto();
        addressBookDto.setId(addressBook.getId());
        addressBookDto.setBranchManager(addressBook.getBranchManager());
        addressBookDto.setType(addressBook.getType());

        if (!ObjectUtils.isEmpty(addressBook.getContacts())) {
            // Convert Contact entities to ContactDto objects
            Set<com.reece.addressbook.dto.ContactDto> contactDtos = addressBook.getContacts().stream()
                    .map(contactMapper::toContactDto)
                    .collect(Collectors.toCollection(HashSet::new));
            addressBookDto.setContacts(contactDtos);
        }
        
        addressBookDto.setCreatedDate(addressBook.getCreatedDate());
        addressBookDto.setUpdatedDate(addressBook.getUpdatedDate());

        return addressBookDto;
    }

}
