package com.reece.addressbook.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setRequestURI("/api/v1/test");
        request = mockRequest;
    }

    @Test
    void handleNotFoundReturns404() {
        ResponseEntityResult result = ResponseEntityResult.of(
                handler.handleNotFound(new ResourceNotFoundException("Address book not found"), request));

        assertThat(result.status).isEqualTo(404);
        assertThat(result.error.getMessage()).isEqualTo("Address book not found");
        assertThat(result.error.getPath()).isEqualTo("/api/v1/test");
        assertThat(result.error.getTimestamp()).isNotNull();
    }

    @Test
    void handleBusinessReturns400() {
        ResponseEntityResult result = ResponseEntityResult.of(
                handler.handleBusiness(new BusinessValidationException("Invalid contact"), request));

        assertThat(result.status).isEqualTo(400);
        assertThat(result.error.getMessage()).isEqualTo("Invalid contact");
        assertThat(result.error.getPath()).isEqualTo("/api/v1/test");
    }

    @Test
    void handleValidationReturnsJoinedFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "branchManager", "Branch manager is required"));
        bindingResult.addError(new FieldError("request", "contacts[0].phoneNumber", "Phone number is required"));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntityResult result = ResponseEntityResult.of(handler.handleValidation(ex, request));

        assertThat(result.status).isEqualTo(400);
        assertThat(result.error.getMessage())
                .isEqualTo("branchManager Branch manager is required, contacts[0].phoneNumber Phone number is required");
    }

    @Test
    void handleValidationFallsBackToDefaultMessageWhenThereAreNoFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(new BeanPropertyBindingResult(new Object(), "request"));

        ResponseEntityResult result = ResponseEntityResult.of(handler.handleValidation(ex, request));

        assertThat(result.status).isEqualTo(400);
        assertThat(result.error.getMessage()).isEqualTo("Validation error");
    }

    @Test
    void handleHttpMessageNotReadableReturnsBadRequest() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("Malformed JSON payload");

        ResponseEntityResult result = ResponseEntityResult.of(handler.handleHttpMessageNotReadable(ex, request));

        assertThat(result.status).isEqualTo(400);
        assertThat(result.error.getMessage()).isEqualTo("Malformed JSON request or invalid input format");
        assertThat(result.error.getPath()).isEqualTo("/api/v1/test");
    }

    @Test
    void handleConstraintViolationReturnsBadRequest() {
        ConstraintViolationException ex = new ConstraintViolationException("page must be greater than or equal to 0", Set.of());

        ResponseEntityResult result = ResponseEntityResult.of(handler.handleConstraintAndTypeMismatch(ex, request));

        assertThat(result.status).isEqualTo(400);
        assertThat(result.error.getMessage()).isEqualTo("page must be greater than or equal to 0");
    }

    @Test
    void handleTypeMismatchReturnsBadRequest() {
        var ex = mock(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class);
        when(ex.getMessage()).thenReturn("Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long' for property 'id'");

        ResponseEntityResult result = ResponseEntityResult.of(handler.handleConstraintAndTypeMismatch(ex, request));

        assertThat(result.status).isEqualTo(400);
        assertThat(result.error.getPath()).isEqualTo("/api/v1/test");
        assertThat(result.error.getMessage()).contains("id");
    }

    @Test
    void handleGenericReturnsInternalServerError() {
        ResponseEntityResult result = ResponseEntityResult.of(
                handler.handleGeneric(new RuntimeException("Unexpected failure"), request));

        assertThat(result.status).isEqualTo(500);
        assertThat(result.error.getMessage()).isEqualTo("Internal server error");
        assertThat(result.error.getPath()).isEqualTo("/api/v1/test");
    }

    private record ResponseEntityResult(int status, ApiError error) {
        static ResponseEntityResult of(org.springframework.http.ResponseEntity<ApiError> response) {
            return new ResponseEntityResult(response.getStatusCode().value(), response.getBody());
        }
    }
}

