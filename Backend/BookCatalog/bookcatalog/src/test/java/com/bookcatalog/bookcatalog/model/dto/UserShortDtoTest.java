package com.bookcatalog.bookcatalog.model.dto;

import com.bookcatalog.bookcatalog.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserShortDtoTest {

    private Role role;

    @Test
    public void testNoArgsConstructor() {
        UserShortDto userShortDto = new UserShortDto();

        assertNull(userShortDto.getUsername());
        assertNull(userShortDto.getEmail());
        assertNull(userShortDto.getRole());
    }

    @Test
    public void testAllArgsConstructor() {
        UserShortDto userShortDto = new UserShortDto("john_doe", "john.doe@example.com", Role.ADMIN);

        assertEquals("john_doe", userShortDto.getUsername());
        assertEquals("john.doe@example.com", userShortDto.getEmail());
        assertEquals(Role.ADMIN, userShortDto.getRole());
    }

    @Test
    public void testSettersAndGetters() {
        UserShortDto userShortDto = new UserShortDto();

        userShortDto.setUsername("jane_doe");
        userShortDto.setEmail("jane.doe@example.com");
        userShortDto.setRole(Role.ADMIN);

        assertEquals("jane_doe", userShortDto.getUsername());
        assertEquals("jane.doe@example.com", userShortDto.getEmail());
        assertEquals(Role.ADMIN, userShortDto.getRole());
    }
}