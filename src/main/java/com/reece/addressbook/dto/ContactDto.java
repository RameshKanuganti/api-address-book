package com.reece.addressbook.dto;

import com.reece.addressbook.model.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ContactDto extends BaseEntity {

    private Long id;

    @NotBlank(message = "Contact name is required")
    private String name;

    @NotNull(message = "Phone number is required")
    private Long phoneNumber;

}