package com.reece.addressbook.mapper;

import com.reece.addressbook.dto.ContactDto;
import com.reece.addressbook.model.Contact;
import org.springframework.stereotype.Component;

@Component
public class ContactMapper {

    public Contact toContactEntity(ContactDto dto) {

        Contact contact = new Contact();
        contact.setId(dto.getId());
        contact.setName(dto.getName());
        contact.setPhoneNumber(dto.getPhoneNumber().trim().replaceAll("\\s+", ""));
        return contact;
    }

    public ContactDto toContactDto(Contact contact) {

        ContactDto contactDto = new ContactDto();
        contactDto.setId(contact.getId());
        contactDto.setName(contact.getName());
        contactDto.setPhoneNumber(contact.getPhoneNumber());
        contactDto.setCreatedDate(contact.getCreatedDate());
        contactDto.setUpdatedDate(contact.getUpdatedDate());

        return contactDto;
    }

}


