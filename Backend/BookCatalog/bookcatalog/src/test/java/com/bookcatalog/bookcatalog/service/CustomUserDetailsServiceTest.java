package com.bookcatalog.bookcatalog.service;

import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

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
    void loadUserByUsername_userFoundByUsername() {
        // Arrange
        String username = "testUser";
        User mockUser = new User();
        mockUser.setUsername(username);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        // Act
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        verify(userRepository, times(1)).findByUsername(username);
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void loadUserByUsername_userNotFound() {
        // Arrange
        String identifier = "unknownUser";
        when(userRepository.findByUsername(identifier)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.empty());

        // Act and Assert
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername(identifier));

        assertEquals("User not found with username : " + identifier, exception.getMessage());
        verify(userRepository, times(1)).findByUsername(identifier);

    }
}