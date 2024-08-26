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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeleteBookByTitleStrategyTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private DeleteBookByTitleStrategy deleteBookByTitleStrategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void delete_shouldThrowEntityNotFoundExceptionWhenBookNotFoundByTitle() {
        // Arrange
        Book book = new Book();
        book.setTitle("Non-Existent Title");

        when(bookRepository.findBookByTitle(book.getTitle())).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            deleteBookByTitleStrategy.delete(book);
        });

        assertEquals("Book with title Non-Existent Title not found", exception.getMessage());
        verify(bookRepository, never()).delete(book);
    }

    @Test
    void delete_shouldCallRepositoryDeleteWhenBookIsFoundByTitle() throws IOException {
        // Arrange
        Book book = new Book();
        book.setTitle("Existing Title");

        when(bookRepository.findBookByTitle(book.getTitle())).thenReturn(Optional.of(book));

        // Act
        deleteBookByTitleStrategy.delete(book);

        // Assert
        verify(bookRepository, times(1)).delete(book);
    }

    @Test
    void delete_shouldThrowRuntimeExceptionWhenRepositoryThrowsAnyOtherException() {
        // Arrange
        Book book = new Book();
        book.setTitle("Existing Title");

        when(bookRepository.findBookByTitle(book.getTitle())).thenReturn(Optional.of(book));
        doThrow(new RuntimeException("Unexpected error")).when(bookRepository).delete(book);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            deleteBookByTitleStrategy.delete(book);
        });

        assertEquals("An error occurred while deleting the book", exception.getMessage());
        verify(bookRepository, times(1)).delete(book);
    }
}
