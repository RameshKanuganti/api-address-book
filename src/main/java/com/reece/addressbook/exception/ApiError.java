package com.reece.addressbook.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class ApiError {

    private final OffsetDateTime timestamp;
    private final String message;
    private final String path;

    public ApiError(String message, String path) {
        this.timestamp = OffsetDateTime.now();
        this.message = message;
        this.path = path;
    }
}