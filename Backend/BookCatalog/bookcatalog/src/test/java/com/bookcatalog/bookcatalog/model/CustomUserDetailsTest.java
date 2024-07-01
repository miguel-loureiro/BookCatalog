package com.bookcatalog.bookcatalog.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class CustomUserDetailsTest {

    private User user;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    public void setUp() {
        user = new User("username", "email@example.com", "password", Role.READER);
        customUserDetails = new CustomUserDetails(user);
    }

    @Test
    public void testConstructor() {
        assertNotNull(customUserDetails.getUser());
        assertEquals(user, customUserDetails.getUser());
    }

    @Test
    public void testGetAuthorities() {
        Collection<? extends GrantedAuthority> authorities = customUserDetails.getAuthorities();
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    @Test
    public void testGetPassword() {
        assertEquals("password", customUserDetails.getPassword());
    }

    @Test
    public void testGetUsername() {
        assertEquals("username", customUserDetails.getUsername());
    }

    @Test
    public void testIsAccountNonExpired() {
        assertTrue(customUserDetails.isAccountNonExpired());
    }

    @Test
    public void testIsAccountNonLocked() {
        assertTrue(customUserDetails.isAccountNonLocked());
    }

    @Test
    public void testIsCredentialsNonExpired() {
        assertTrue(customUserDetails.isCredentialsNonExpired());
    }

    @Test
    public void testIsEnabled() {
        assertTrue(customUserDetails.isEnabled());
    }

    @Test
    public void testSetUser() {
        User newUser = new User("newusername", "newemail@example.com", "newpassword", Role.ADMIN);
        customUserDetails.setUser(newUser);

        assertEquals("newusername", customUserDetails.getUsername());
        assertEquals("newpassword", customUserDetails.getPassword());
        assertEquals(newUser, customUserDetails.getUser());
    }
}