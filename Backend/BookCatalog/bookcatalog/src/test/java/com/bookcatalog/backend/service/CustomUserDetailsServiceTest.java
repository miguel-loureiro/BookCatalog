package com.newbookcatalog.newbookcatalog.service;

import com.newbookcatalog.newbookcatalog.model.CustomUserDetails;
import com.newbookcatalog.newbookcatalog.model.Role;
import com.newbookcatalog.newbookcatalog.model.User;
import com.newbookcatalog.newbookcatalog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLoadUserByUsername_GuestUser() {
        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("guestuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals("guestuser", userDetails.getUsername());
        assertInstanceOf(CustomUserDetails.class, userDetails);
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        assertEquals(Role.GUEST, customUserDetails.getUser().getRole());
    }

    @Test
    public void testLoadUserByUsername_ValidUser() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setRole(Role.ADMIN);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertInstanceOf(CustomUserDetails.class, userDetails);
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        assertEquals(Role.ADMIN, customUserDetails.getUser().getRole());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    public void testLoadUserByUsername_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("nonexistentuser")
        );

        assertEquals("User not found with username : nonexistentuser", exception.getMessage());
        verify(userRepository).findByUsername("nonexistentuser");
    }
}