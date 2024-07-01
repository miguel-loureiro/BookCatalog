package com.bookcatalog.bookcatalog.model;

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
    void setToken() {
        loginResponse.setToken("newToken");
        assertEquals("newToken", loginResponse.getToken());
    }

    @Test
    void testGetExpiresIn() {
        loginResponse.setExpiresIn(3600);
        assertEquals(3600, loginResponse.getExpiresIn());
    }

    @Test
    void testSetExpiresIn() {
        loginResponse.setExpiresIn(7200);
        assertEquals(7200, loginResponse.getExpiresIn());
    }
}