package com.bookcatalog.bookcatalog.service.strategy.delete;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DeleteBookByIdStrategyTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private DeleteBookByIdStrategy deleteBookByIdStrategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void delete_shouldThrowEntityNotFoundExceptionWhenEntityIsNull() {
        // Act and Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            deleteBookByIdStrategy.delete(null);
        });

        assertEquals("Book not found", exception.getMessage());
        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    void delete_shouldThrowEntityNotFoundExceptionWhenEntityIdIsNull() {
        // Arrange
        Book book = new Book();
        book.setId(null);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            deleteBookByIdStrategy.delete(book);
        });

        assertEquals("Book not found", exception.getMessage());
        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    void delete_shouldCallRepositoryDeleteWhenEntityIsValid() throws IOException {
        // Arrange
        Book book = new Book();
        book.setId(1);

        // Act
        deleteBookByIdStrategy.delete(book);

        // Assert
        verify(bookRepository, times(1)).delete(book);
    }

    @Test
    void delete_shouldThrowEntityNotFoundExceptionWhenRepositoryThrowsEntityNotFoundException() {
        // Arrange
        Book book = new Book();
        book.setId(1);

        doThrow(new EntityNotFoundException("Book with ID 1 not found")).when(bookRepository).delete(book);

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            deleteBookByIdStrategy.delete(book);
        });

        assertEquals("Book with ID 1 not found", exception.getMessage());
        verify(bookRepository, times(1)).delete(book);
    }

    @Test
    void delete_shouldThrowRuntimeExceptionWhenRepositoryThrowsAnyOtherException() {
        // Arrange
        Book book = new Book();
        book.setId(1);

        doThrow(new RuntimeException("Unexpected error")).when(bookRepository).delete(book);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deleteBookByIdStrategy.delete(book);
        });

        assertEquals("An error occurred while deleting the book", exception.getMessage());
        verify(bookRepository, times(1)).delete(book);
    }
}