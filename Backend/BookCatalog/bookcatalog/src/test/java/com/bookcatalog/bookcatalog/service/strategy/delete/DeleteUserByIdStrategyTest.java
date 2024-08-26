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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeleteUserByIdStrategyTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DeleteUserByIdStrategy deleteUserByIdStrategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void delete_shouldThrowEntityNotFoundExceptionWhenUserIsNull() {
        // Act and Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            deleteUserByIdStrategy.delete(null);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void delete_shouldThrowEntityNotFoundExceptionWhenUserIdIsNull() {
        // Arrange
        User user = new User();

        // Act and Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            deleteUserByIdStrategy.delete(user);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void delete_shouldDeleteUserSuccessfullyWhenValidUserIsProvided() throws IOException {
        // Arrange
        User user = new User();
        user.setId(1);

        doNothing().when(userRepository).delete(user);

        // Act
        deleteUserByIdStrategy.delete(user);

        // Assert
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void delete_shouldThrowEntityNotFoundExceptionWhenEntityNotFoundDuringDeletion() {
        // Arrange
        User user = new User();
        user.setId(1);

        doThrow(new EntityNotFoundException("User with ID 1 not found")).when(userRepository).delete(user);

        // Act and Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            deleteUserByIdStrategy.delete(user);
        });

        assertEquals("User with ID 1 not found", exception.getMessage());
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void delete_shouldThrowRuntimeExceptionWhenGeneralExceptionOccursDuringDeletion() {
        // Arrange
        User user = new User();
        user.setId(1);

        doThrow(new RuntimeException("Unexpected error")).when(userRepository).delete(user);

        // Act and Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deleteUserByIdStrategy.delete(user);
        });

        assertEquals("An error occurred while deleting the user", exception.getMessage());
        verify(userRepository, times(1)).delete(user);
    }
}