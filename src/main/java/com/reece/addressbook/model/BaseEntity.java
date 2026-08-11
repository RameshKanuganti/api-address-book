package com.reece.addressbook.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.sql.Timestamp;

@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
@Getter
@Setter
public class BaseEntity implements Serializable {

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Timestamp createdDate;

    @UpdateTimestamp
    @Column(nullable = false)
    private Timestamp updatedDate;

}