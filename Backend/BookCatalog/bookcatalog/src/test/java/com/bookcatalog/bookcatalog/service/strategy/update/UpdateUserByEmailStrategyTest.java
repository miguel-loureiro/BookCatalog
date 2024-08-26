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

class UpdateUserByEmailStrategyTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdateUserByEmailStrategy updateUserByEmailStrategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void update_shouldThrowEntityNotFoundExceptionWhenUserNotFoundByEmail() {
        // Arrange
        User existingUser = new User();
        existingUser.setEmail("nonexistent@example.com");

        User newDetails = new User();
        newDetails.setEmail("newemail@example.com");

        when(userRepository.findByEmail(existingUser.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            updateUserByEmailStrategy.update(existingUser, newDetails, null);
        });

        assertEquals("User with email nonexistent@example.com not found", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_shouldUpdateUserSuccessfullyWhenUserExists() throws IOException {
        // Arrange
        User existingUser = new User();
        existingUser.setEmail("existing@example.com");
        existingUser.setUsername("oldUsername");
        existingUser.setRole(Role.READER);

        User newDetails = new User();
        newDetails.setEmail("newemail@example.com");
        newDetails.setUsername("newUsername");
        newDetails.setRole(Role.ADMIN);

        when(userRepository.findByEmail(existingUser.getEmail())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(existingUser);

        // Act
        User updatedUser = updateUserByEmailStrategy.update(existingUser, newDetails, null);

        // Assert
        assertNotNull(updatedUser);
        assertEquals("newUsername", updatedUser.getUsername());
        assertEquals("newemail@example.com", updatedUser.getEmail());
        assertEquals(Role.ADMIN, updatedUser.getRole());
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void update_shouldThrowIOExceptionWhenExceptionOccursDuringUpdate() {
        // Arrange
        User existingUser = new User();
        existingUser.setEmail("existing@example.com");

        User newDetails = new User();
        newDetails.setEmail("newemail@example.com");

        when(userRepository.findByEmail(existingUser.getEmail())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            updateUserByEmailStrategy.update(existingUser, newDetails, null);
        });

        assertEquals("Database error", exception.getMessage());
        verify(userRepository, times(1)).save(existingUser);
    }
}