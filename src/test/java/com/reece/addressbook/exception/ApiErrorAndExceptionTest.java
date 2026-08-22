package com.reece.addressbook.exception;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ApiErrorAndExceptionTest {

    @Test
    void apiErrorTwoArgConstructorPopulatesTimestampMessageAndPath() {
        ApiError apiError = new ApiError("Something went wrong", "/api/v1/test");

        assertThat(apiError.getTimestamp()).isNotNull();
        assertThat(apiError.getMessage()).isEqualTo("Something went wrong");
        assertThat(apiError.getPath()).isEqualTo("/api/v1/test");
    }

    @Test
    void apiErrorAllArgsConstructorUsesProvidedValues() {
        OffsetDateTime timestamp = OffsetDateTime.parse("2026-08-22T12:00:00Z");
        ApiError apiError = new ApiError(timestamp, "Explicit message", "/path");

        assertThat(apiError.getTimestamp()).isEqualTo(timestamp);
        assertThat(apiError.getMessage()).isEqualTo("Explicit message");
        assertThat(apiError.getPath()).isEqualTo("/path");
    }

    @Test
    void customExceptionsPreserveMessages() {
        BusinessValidationException business = new BusinessValidationException("Business rule failed");
        ResourceNotFoundException notFound = new ResourceNotFoundException("Missing resource");

        assertThat(business).hasMessage("Business rule failed");
        assertThat(notFound).hasMessage("Missing resource");
    }
}

