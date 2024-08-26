package com.bookcatalog.bookcatalog.service.strategy.delete;

import static org.junit.jupiter.api.Assertions.*;
import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.io.IOException;
import java.util.Optional;
import static org.mockito.Mockito.*;

class DeleteBookByISBNStrategyTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private DeleteBookByISBNStrategy deleteBookByISBNStrategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void delete_shouldThrowEntityNotFoundExceptionWhenBookNotFoundByISBN() {
        // Arrange
        Book book = new Book();
        book.setIsbn("1234567890");

        when(bookRepository.findBookByIsbn(book.getIsbn())).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            deleteBookByISBNStrategy.delete(book);
        });

        assertEquals("Book with ISBN 1234567890 not found", exception.getMessage());
        verify(bookRepository, never()).delete(book);
    }

    @Test
    void delete_shouldCallRepositoryDeleteWhenBookIsFoundByISBN() throws IOException {
        // Arrange
        Book book = new Book();
        book.setIsbn("1234567890");

        when(bookRepository.findBookByIsbn(book.getIsbn())).thenReturn(Optional.of(book));

        // Act
        deleteBookByISBNStrategy.delete(book);

        // Assert
        verify(bookRepository, times(1)).delete(book);
    }

    @Test
    void delete_shouldThrowRuntimeExceptionWhenRepositoryThrowsAnyOtherException() {
        // Arrange
        Book book = new Book();
        book.setIsbn("1234567890");

        when(bookRepository.findBookByIsbn(book.getIsbn())).thenReturn(Optional.of(book));
        doThrow(new RuntimeException("Unexpected error")).when(bookRepository).delete(book);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deleteBookByISBNStrategy.delete(book);
        });

        assertEquals("An error occurred while deleting the book", exception.getMessage());
        verify(bookRepository, times(1)).delete(book);
    }
}