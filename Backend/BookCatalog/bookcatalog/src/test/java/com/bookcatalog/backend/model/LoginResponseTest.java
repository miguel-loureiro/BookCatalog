package com.newbookcatalog.newbookcatalog.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginResponseTest {

    private LoginResponse loginResponse;

    @BeforeEach
    public void setUp() {
        loginResponse = new LoginResponse();
    }

    @Test
    void getToken() {
        loginResponse.setToken("newToken");
        assertEquals("newToken", loginResponse.getToken());
    }

    @Test
    public void testSetTokenValid() {
        LoginResponse loginResponse = new LoginResponse();
        String validToken = "validToken123";
        LoginResponse result = loginResponse.setToken(validToken);
        assertEquals(validToken, loginResponse.getToken());
        assertNotNull(result);
    }

    @Test
    public void testSetTokenThrowsExceptionOnNull() {
        LoginResponse loginResponse = new LoginResponse();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            loginResponse.setToken(null);
        });
        assertEquals("Token cannot be null", exception.getMessage());
    }

    @Test
    public void testSetTokenReturnType() {
        LoginResponse loginResponse = new LoginResponse();
        String validToken = "validToken123";
        assertDoesNotThrow(() -> {
            LoginResponse result = loginResponse.setToken(validToken);
            assertNotNull(result, "setToken should never return null");
            assertEquals(validToken, loginResponse.getToken(), "Token should be set correctly");
        });
    }

    @Test
    public void testSetExpiresInThrowsExceptionOnNull() {
        LoginResponse loginResponse = new LoginResponse();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            loginResponse.setExpiresIn(null);
        });
        assertEquals("ExpiresIn cannot be null", exception.getMessage());
    }

    @Test
    public void testSetExpiresInThrowsExceptionOnZero() {
        LoginResponse loginResponse = new LoginResponse();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            loginResponse.setExpiresIn(0L);
        });
        assertEquals("expiresIn must be positive", exception.getMessage());
    }

    @Test
    public void testSetExpiresInThrowsExceptionOnNegativeValue() {
        LoginResponse loginResponse = new LoginResponse();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            loginResponse.setExpiresIn(-3600L);
        });
        assertEquals("expiresIn must be positive", exception.getMessage());
    }

    @Test
    public void testSetExpiresInReturnType() {
        LoginResponse loginResponse = new LoginResponse();
        Long validExpiresIn = 3600L;
        assertDoesNotThrow(() -> {
            LoginResponse result = loginResponse.setExpiresIn(validExpiresIn);
            assertNotNull(result, "setExpiresIn should never return null");
            assertEquals(validExpiresIn.longValue(), loginResponse.getExpiresIn(), "expiresIn should be set correctly");
        });
    }


    @Test
    void testGetExpiresIn() {
        loginResponse.setExpiresIn(3600L);
        assertEquals(3600L, loginResponse.getExpiresIn());
    }

    @Test
    void testSetExpiresIn() {
        loginResponse.setExpiresIn(7200L);
        assertEquals(7200L, loginResponse.getExpiresIn());
    }
}