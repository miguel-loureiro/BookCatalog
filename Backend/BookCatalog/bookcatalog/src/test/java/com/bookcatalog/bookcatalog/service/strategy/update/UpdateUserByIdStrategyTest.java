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
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UpdateUserByIdStrategyTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UpdateUserByIdStrategy updateUserByIdStrategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void update_shouldThrowEntityNotFoundExceptionWhenUserIsNull() {
        // Arrange
        User newDetails = new User();
        newDetails.setEmail("newemail@example.com");

        // Act and Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            updateUserByIdStrategy.update(null, newDetails, null);
        });

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_shouldThrowIllegalArgumentExceptionWhenNewDetailsIsNull() {
        // Arrange
        User user = new User();

        // Act and Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            updateUserByIdStrategy.update(user, null, null);
        });

        assertEquals("New details cannot be null", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_shouldUpdateUserSuccessfullyWhenValidInputsAreProvided() throws IOException {
        // Arrange
        User user = new User();
        user.setId(1);

        User newDetails = new User();
        newDetails.setEmail("newemail@example.com");
        newDetails.setPassword("newpassword");
        newDetails.setRole(Role.READER);
        newDetails.setBooks(new HashSet<>());

        when(userRepository.save(user)).thenReturn(user);

        // Act
        User updatedUser = updateUserByIdStrategy.update(user, newDetails, null);

        // Assert
        assertEquals("newemail@example.com", updatedUser.getEmail());
        assertEquals("newpassword", updatedUser.getPassword());
        assertEquals(Role.READER, updatedUser.getRole());
        assertNotNull(updatedUser.getBooks());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void update_shouldThrowRuntimeExceptionWhenSaveFails() {
        // Arrange
        User user = new User();
        user.setId(1);

        User newDetails = new User();
        newDetails.setEmail("newemail@example.com");
        newDetails.setPassword("newpassword");
        newDetails.setRole(Role.READER);

        when(userRepository.save(user)).thenThrow(new RuntimeException("Unexpected error"));

        // Act and Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            updateUserByIdStrategy.update(user, newDetails, null);
        });

        assertEquals("An error occurred while updating the user", exception.getMessage());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void update_shouldUpdateUserWithFilenameWhenProvided() throws IOException {
        // Arrange
        User user = new User();
        user.setId(1);

        User newDetails = new User();
        newDetails.setEmail("newemail@example.com");
        newDetails.setPassword("newpassword");
        newDetails.setRole(Role.READER);
        newDetails.setBooks(new HashSet<>());

        String filename = "new_profile_picture.jpg";

        when(userRepository.save(user)).thenReturn(user);

        // Act
        User updatedUser = updateUserByIdStrategy.update(user, newDetails, filename);

        // Assert
        assertEquals("newemail@example.com", updatedUser.getEmail());
        assertEquals("newpassword", updatedUser.getPassword());
        assertEquals(Role.READER, updatedUser.getRole());
        assertNotNull(updatedUser.getBooks());
        verify(userRepository, times(1)).save(user);
    }
}