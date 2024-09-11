package com.bookcatalog.bookcatalog.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationResponseTest {

    private User user;
    private String token;
    private AuthenticationResponse authenticationResponse;

    @BeforeEach
    public void setUp() {

        user = new User("username", "email@example.com", "password", Role.READER);
        token = "mocked_token";
        authenticationResponse = new AuthenticationResponse(user, token);
    }

    @Test
    public void testConstructorAndGetters() {
        assertEquals(user, authenticationResponse.getUser());
        assertEquals(token, authenticationResponse.getToken());
    }

    @Test
    public void testSetters() {
        User newUser = new User("newusername", "newemail@example.com", "newpassword", Role.ADMIN);
        String newToken = "new_mocked_token";

        authenticationResponse.setUser(newUser);
        authenticationResponse.setToken(newToken);

        assertEquals(newUser, authenticationResponse.getUser());
        assertEquals(newToken, authenticationResponse.getToken());
    }

    @Test
    public void testNullUserInConstructor() {
        AuthenticationResponse response = new AuthenticationResponse(null, "token");
        assertNotNull(response);
        assertNull(response.getUser());
        assertEquals("token", response.getToken());
    }

    @Test
    public void testNullTokenInConstructor() {
        AuthenticationResponse response = new AuthenticationResponse(user, null);
        assertNotNull(response);
        assertEquals(user, response.getUser());
        assertNull(response.getToken());
    }
}