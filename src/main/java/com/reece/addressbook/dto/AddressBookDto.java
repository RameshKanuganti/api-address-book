package com.reece.addressbook.dto;

import com.reece.addressbook.model.AddressBookType;
import com.reece.addressbook.model.BaseEntity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class AddressBookDto extends BaseEntity {

    private Long id;

    @NotBlank(message = "Branch manager is required")
    @Size(max = 100, message = "Branch manager name must not exceed 100 characters")
    private String branchManager;

    private AddressBookType type;

    @Valid
    private Set<ContactDto> contacts = new HashSet<>();

}