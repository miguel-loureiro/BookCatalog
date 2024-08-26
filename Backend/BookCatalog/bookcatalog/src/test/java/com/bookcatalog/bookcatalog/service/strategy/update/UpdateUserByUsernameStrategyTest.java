package com.bookcatalog.bookcatalog.service.strategy.update;


import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UpdateUserByUsernameStrategyTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdateUserByUsernameStrategy updateUserByUsernameStrategy;

    private User existingUser;
    private User newUserDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup some mock users
        existingUser = new User();
        existingUser.setId(1);
        existingUser.setUsername("existingUser");
        existingUser.setEmail("existing@domain.com");
        existingUser.setRole(Role.READER);

        newUserDetails = new User();
        newUserDetails.setUsername("newUsername");
        newUserDetails.setEmail("newemail@domain.com");
        newUserDetails.setRole(Role.ADMIN);
    }

    @Test
    public void testUpdateUser_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername(existingUser.getUsername())).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            updateUserByUsernameStrategy.update(existingUser, newUserDetails, null);
        });

        assertEquals("User with username existingUser not found", thrown.getMessage());
        verify(userRepository, times(1)).findByUsername(existingUser.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testUpdateUser_Success() throws IOException {
        // Arrange
        when(userRepository.findByUsername(existingUser.getUsername())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        // Act
        User updatedUser = updateUserByUsernameStrategy.update(existingUser, newUserDetails, null);

        // Assert
        assertNotNull(updatedUser);
        assertEquals(newUserDetails.getUsername(), updatedUser.getUsername());
        assertEquals(newUserDetails.getEmail(), updatedUser.getEmail());
        assertEquals(newUserDetails.getRole(), updatedUser.getRole());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    public void testUpdateUser_SaveFailure() throws IOException {
        // Arrange
        when(userRepository.findByUsername(existingUser.getUsername())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenThrow(new RuntimeException("Database save error"));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            updateUserByUsernameStrategy.update(existingUser, newUserDetails, null);
        });

        assertEquals("Database save error", thrown.getMessage());
        verify(userRepository, times(1)).save(existingUser);
    }
}
