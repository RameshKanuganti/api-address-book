package com.reece.addressbook.dto;

import com.reece.addressbook.model.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class ContactDto extends BaseEntity {

    private Long id;

    @NotBlank(message = "Contact name is required")
    @Size(max = 30, message = "Contact name must not exceed 30 characters")
    private String name;

    /**
     * Phone number in E.164 format (e.g. +61412345678).
     * Using String instead of Long to correctly represent leading zeros,
     * country codes, and international formatting.
     */
    @NotBlank(message = "Phone number is required")
    @Size(max = 15, message = "Phone number must not exceed 15 characters")
    @Pattern(
            regexp = "^\\+[1-9]\\d{0,2}(\\s?\\d+)+$",
            message = "Phone number must start with a country code and may contain spaces"
    )
    private String phoneNumber;
}