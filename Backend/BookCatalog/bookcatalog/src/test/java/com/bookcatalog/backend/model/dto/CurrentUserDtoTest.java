package com.newbookcatalog.newbookcatalog.model.dto;

import com.newbookcatalog.newbookcatalog.model.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurrentUserDtoTest {

    @Test
    public void testObjectCreationAndGettersSetters() {
        // Create an instance of CurrentUserDto
        CurrentUserDto currentUserDto = new CurrentUserDto("testUser", "test@example.com", Role.ADMIN);

        // Validate using getters
        assertEquals("testUser", currentUserDto.getUsername());
        assertEquals("test@example.com", currentUserDto.getEmail());
        assertEquals(Role.ADMIN, currentUserDto.getRole());

        // Set new values using setters
        currentUserDto.setUsername("newUser");
        currentUserDto.setEmail("new@example.com");
        currentUserDto.setRole(Role.ADMIN);

        // Validate using getters
        assertEquals("newUser", currentUserDto.getUsername());
        assertEquals("new@example.com", currentUserDto.getEmail());
        assertEquals(Role.ADMIN, currentUserDto.getRole());
    }

    @Test
    public void testDefaultValues() {
        // Create an instance of CurrentUserDto
        CurrentUserDto currentUserDto = new CurrentUserDto(null, null, null);

        // Validate that all fields are initially null
        assertNull(currentUserDto.getUsername());
        assertNull(currentUserDto.getEmail());
        assertNull(currentUserDto.getRole());
    }
}