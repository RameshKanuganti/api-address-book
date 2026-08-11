package com.reece.addressbook.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Contact entity with business-identity-based equality.
 * equals/hashCode are deliberately based on {@code id} (stable after persist)
 * to ensure correct behaviour when Contact objects are stored in JPA-managed
 * HashSet collections inside AddressBook.
 *
 * Phone number is stored as a String (E.164 format) to support leading zeros,
 * country codes, and international formatting.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Contact extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** E.164 format – e.g. +61412345678. Unique across all contacts. */
    @Column(nullable = false, unique = true)
    private String phoneNumber;

    // ----- JPA-safe equals / hashCode -----

    /**
     * Equality is based solely on the database id so that two managed
     * entity references that point to the same row are considered equal,
     * while two transient instances (id == null) are never equal to each
     * other – which is safe for Set membership before persist.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Contact other)) return false;
        return id != null && id.equals(other.id);
    }

    /**
     * Constant hash code so the entity can be moved between a transient and
     * a managed state without invalidating its Set membership.
     */
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
