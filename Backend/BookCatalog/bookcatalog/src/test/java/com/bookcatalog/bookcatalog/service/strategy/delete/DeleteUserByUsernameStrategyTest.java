package com.bookcatalog.bookcatalog.service.strategy.delete;

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

class DeleteUserByUsernameStrategyTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeleteUserByUsernameStrategy deleteUserByUsernameStrategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void delete_shouldThrowEntityNotFoundExceptionWhenUserNotFoundByUsername() {
        // Arrange
        User user = new User();
        user.setUsername("nonexistentUser");

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            deleteUserByUsernameStrategy.delete(user);
        });

        assertEquals("User with username nonexistentUser not found", exception.getMessage());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void delete_shouldDeleteUserSuccessfullyWhenUserExists() throws IOException {
        // Arrange
        User user = new User();
        user.setUsername("existingUser");

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        // Act
        deleteUserByUsernameStrategy.delete(user);

        // Assert
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void delete_shouldThrowRuntimeExceptionWhenExceptionOccursDuringDeletion() {
        // Arrange
        User user = new User();
        user.setUsername("existingUser");

        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("Unexpected error")).when(userRepository).delete(user);

        // Act and Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deleteUserByUsernameStrategy.delete(user);
        });

        assertEquals("An error occurred while deleting the user", exception.getMessage());
        verify(userRepository, times(1)).delete(user);
    }
}