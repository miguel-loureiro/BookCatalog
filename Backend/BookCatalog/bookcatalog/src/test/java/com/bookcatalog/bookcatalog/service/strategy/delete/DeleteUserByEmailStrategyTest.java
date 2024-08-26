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

class DeleteUserByEmailStrategyTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeleteUserByEmailStrategy deleteUserByEmailStrategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void delete_shouldThrowEntityNotFoundExceptionWhenUserNotFoundByEmail() {
        // Arrange
        User user = new User();
        user.setEmail("nonexistent@example.com");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        // Act and Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            deleteUserByEmailStrategy.delete(user);
        });

        assertEquals("User with email nonexistent@example.com not found", exception.getMessage());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void delete_shouldDeleteUserSuccessfullyWhenUserExists() throws IOException {
        // Arrange
        User user = new User();
        user.setEmail("existing@example.com");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        // Act
        deleteUserByEmailStrategy.delete(user);

        // Assert
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void delete_shouldThrowRuntimeExceptionWhenExceptionOccursDuringDeletion() {
        // Arrange
        User user = new User();
        user.setEmail("existing@example.com");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("Unexpected error")).when(userRepository).delete(user);

        // Act and Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deleteUserByEmailStrategy.delete(user);
        });

        assertEquals("An error occurred while deleting the user", exception.getMessage());
        verify(userRepository, times(1)).delete(user);
    }
}