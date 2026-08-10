package com.reece.addressbook.mapper;

import com.reece.addressbook.dto.AddressBookDto;
import com.reece.addressbook.mapper.AddressBookMapper;
import com.reece.addressbook.model.AddressBook;
import com.reece.addressbook.model.AddressBookType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class AddressBookMapperTest {

    @Autowired
    private AddressBookMapper addressBookMapper;

    private AddressBook addressBook;
    private AddressBookDto addressBookDto;

    @BeforeEach
    void setUp() {
        addressBook = new AddressBook();
        addressBook.setId(1L);
        addressBook.setBranchManager("Manager1");
        addressBook.setType(AddressBookType.DOMESTIC);
        addressBook.setContacts(new HashSet<>());

        addressBookDto = new AddressBookDto();
        addressBookDto.setId(1L);
        addressBookDto.setBranchManager("Manager1");
        addressBookDto.setType(AddressBookType.DOMESTIC);
        addressBookDto.setContacts(new HashSet<>());
    }

    @Test
    void mapAddressBookToAddressBookDto() {
        AddressBook ab = new AddressBook();
        ab.setId(1L);
        ab.setBranchManager("Manager1");
        ab.setType(AddressBookType.INDUSTRIAL);
        ab.setContacts(new HashSet<>());

        AddressBookDto result = addressBookMapper.toAddressBookDto(ab);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ab.getId());
        assertThat(result.getBranchManager()).isEqualTo(ab.getBranchManager());
        assertThat(result.getType()).isEqualTo(ab.getType());
    }

    @Test
    void mapAddressBookDtoToAddressBook() {
        AddressBookDto dto = new AddressBookDto();
        dto.setId(1L);
        dto.setBranchManager("Manager1");
        dto.setType(AddressBookType.IRRIGATION);
        dto.setContacts(new HashSet<>());

        AddressBook result = addressBookMapper.toAddressBookEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(dto.getId());
        assertThat(result.getBranchManager()).isEqualTo(dto.getBranchManager());
        assertThat(result.getType()).isEqualTo(dto.getType());
    }

    @Test
    void mapAddressBookWithAllTypes() {
        for (AddressBookType type : AddressBookType.values()) {
            AddressBook ab = new AddressBook();
            ab.setId(1L);
            ab.setBranchManager("Manager_" + type.name());
            ab.setType(type);
            ab.setContacts(new HashSet<>());

            AddressBookDto result = addressBookMapper.toAddressBookDto(ab);

            assertThat(result.getType()).isEqualTo(type);
        }
    }

    @Test
    void mapAddressBookDtoWithNullType() {
        AddressBookDto dto = new AddressBookDto();
        dto.setId(1L);
        dto.setBranchManager("Manager1");
        dto.setType(null);
        dto.setContacts(new HashSet<>());

        AddressBook result = addressBookMapper.toAddressBookEntity(dto);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isNull();
    }

    @Test
    void mapAddressBookDtoWithNullContacts() {
        AddressBookDto dto = new AddressBookDto();
        dto.setId(1L);
        dto.setBranchManager("Manager1");
        dto.setType(AddressBookType.DOMESTIC);
        dto.setContacts(null);

        AddressBook result = addressBookMapper.toAddressBookEntity(dto);

        assertThat(result).isNotNull();
    }

    @Test
    void roundTripAddressBookMapping() {
        AddressBook original = new AddressBook();
        original.setId(1L);
        original.setBranchManager("Test Manager");
        original.setType(AddressBookType.FINANCIAL);
        original.setContacts(new HashSet<>());

        AddressBookDto dto = addressBookMapper.toAddressBookDto(original);
        AddressBook mapped = addressBookMapper.toAddressBookEntity(dto);

        assertThat(mapped.getId()).isEqualTo(original.getId());
        assertThat(mapped.getBranchManager()).isEqualTo(original.getBranchManager());
        assertThat(mapped.getType()).isEqualTo(original.getType());
    }

    @Test
    void mapAddressBookWithSpecialCharactersInBranchName() {
        AddressBook ab = new AddressBook();
        ab.setId(1L);
        ab.setBranchManager("Branch & Co. - New York");
        ab.setType(AddressBookType.INFRASTRUCTURE);
        ab.setContacts(new HashSet<>());

        AddressBookDto result = addressBookMapper.toAddressBookDto(ab);

        assertThat(result.getBranchManager()).isEqualTo("Branch & Co. - New York");
    }

    @Test
    void mapAddressBookPreservesId() {
        AddressBook ab = new AddressBook();
        ab.setId(999L);
        ab.setBranchManager("Manager");
        ab.setType(AddressBookType.CIVIL);
        ab.setContacts(new HashSet<>());

        AddressBookDto result = addressBookMapper.toAddressBookDto(ab);

        assertThat(result.getId()).isEqualTo(999L);
    }

    @Test
    void mapMultipleDifferentAddressBookTypes() {
        AddressBook domestic = new AddressBook();
        domestic.setId(1L);
        domestic.setBranchManager("Domestic Manager");
        domestic.setType(AddressBookType.DOMESTIC);
        domestic.setContacts(new HashSet<>());

        AddressBook industrial = new AddressBook();
        industrial.setId(2L);
        industrial.setBranchManager("Industrial Manager");
        industrial.setType(AddressBookType.INDUSTRIAL);
        industrial.setContacts(new HashSet<>());

        AddressBookDto domesticDto = addressBookMapper.toAddressBookDto(domestic);
        AddressBookDto industrialDto = addressBookMapper.toAddressBookDto(industrial);

        assertThat(domesticDto.getType()).isEqualTo(AddressBookType.DOMESTIC);
        assertThat(industrialDto.getType()).isEqualTo(AddressBookType.INDUSTRIAL);
    }
}

