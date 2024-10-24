package com.bookcatalog.backend.exceptions;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.context.request.WebRequest;

import java.security.SignatureException;

import static org.junit.jupiter.api.Assertions.*;

public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    public void setUp() {

        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    public void testHandleBadCredentialsException() {
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");
        ProblemDetail result = globalExceptionHandler.handleSecurityException(exception);

        assertNotNull(result);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getStatus());
        assertEquals("Bad credentials", result.getDetail());
        assertEquals("The username or password is incorrect", result.getProperties().get("description"));
    }

    @Test
    public void testHandleAccountStatusException() {

        AccountStatusException exception = new AccountStatusException("Account is locked") {};
        ProblemDetail result = globalExceptionHandler.handleSecurityException(exception);

        assertNotNull(result);
        assertEquals(HttpStatus.FORBIDDEN.value(), result.getStatus());
        assertEquals("Account is locked", result.getDetail());
        assertEquals("The account is locked", result.getProperties().get("description"));
    }

    @Test
    public void testHandleAccessDeniedException() {

        AccessDeniedException exception = new AccessDeniedException("Access denied");
        ProblemDetail result = globalExceptionHandler.handleSecurityException(exception);

        assertNotNull(result);
        assertEquals(HttpStatus.FORBIDDEN.value(), result.getStatus());
        assertEquals("Access denied", result.getDetail());
        assertEquals("You are not authorized to access this resource", result.getProperties().get("description"));
    }

    @Test
    public void testHandleSignatureException() {

        SignatureException exception = new SignatureException("Invalid signature");
        ProblemDetail result = globalExceptionHandler.handleSecurityException(exception);

        assertNotNull(result);
        assertEquals(HttpStatus.FORBIDDEN.value(), result.getStatus());
        assertEquals("Invalid signature", result.getDetail());
        assertEquals("The JWT signature is invalid", result.getProperties().get("description"));
    }

    @Test
    public void testHandleExpiredJwtException() {

        ExpiredJwtException exception = Mockito.mock(ExpiredJwtException.class);
        Mockito.when(exception.getMessage()).thenReturn("JWT expired");
        ProblemDetail result = globalExceptionHandler.handleSecurityException(exception);

        assertNotNull(result);
        assertEquals(HttpStatus.FORBIDDEN.value(), result.getStatus());
        assertEquals("JWT expired", result.getDetail());
        assertEquals("The JWT token has expired", result.getProperties().get("description"));
    }

    @Test
    public void testHandleUnknownException() {

        Exception exception = new Exception("Unknown error");
        ProblemDetail result = globalExceptionHandler.handleSecurityException(exception);

        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatus());
        assertEquals("Unknown error", result.getDetail());
        assertEquals("Unknown internal server error.", result.getProperties().get("description"));
    }

    @Test
    public void testHandleInvalidUserRoleException() {

        InvalidUserRoleException exception = new InvalidUserRoleException("Invalid role");
        WebRequest request = Mockito.mock(WebRequest.class);
        ResponseEntity<?> result = globalExceptionHandler.handleInvalidUserRoleException(exception, request);

        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Invalid role", result.getBody());
    }
}