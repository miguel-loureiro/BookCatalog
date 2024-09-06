package com.bookcatalog.bookcatalog.service.strategy.update;

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
import static org.mockito.Mockito.*;

class UpdateBookByIdStrategyTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private UpdateBookByIdStrategy updateBookByIdStrategy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void update_shouldThrowEntityNotFoundExceptionWhenBookIsNull() {
        // Arrange
        Book newDetails = new Book();
        newDetails.setTitle("New Title");

        // Act and Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            updateBookByIdStrategy.update(null, newDetails, null);
        });

        assertEquals("Book not found", exception.getMessage());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void update_shouldThrowIllegalArgumentExceptionWhenNewDetailsIsNull() {
        // Arrange
        Book book = new Book();

        // Act and Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            updateBookByIdStrategy.update(book, null, null);
        });

        assertEquals("New details cannot be null", exception.getMessage());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void update_shouldUpdateBookSuccessfullyWhenValidInputsAreProvided() throws IOException {
        // Arrange
        Book book = new Book();
        book.setId(1);

        Book newDetails = new Book();
        newDetails.setTitle("New Title");
        newDetails.setAuthor("New Author");
        newDetails.setIsbn("1234567890");
        newDetails.setPrice("29.99");
        newDetails.setPublishDate("01/2023");

        when(bookRepository.save(book)).thenReturn(book);

        // Act
        Book updatedBook = updateBookByIdStrategy.update(book, newDetails, null);

        // Assert
        assertEquals("New Title", updatedBook.getTitle());
        assertEquals("New Author", updatedBook.getAuthor());
        assertEquals("1234567890", updatedBook.getIsbn());
        assertEquals("29.99", updatedBook.getPrice());
        assertEquals("01/2023", updatedBook.getPublishDate());
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    void update_shouldUpdateBookWithCoverImageWhenFilenameIsProvided() throws IOException {
        // Arrange
        Book book = new Book();
        book.setId(1);

        Book newDetails = new Book();
        newDetails.setTitle("New Title");
        newDetails.setAuthor("New Author");
        newDetails.setIsbn("1234567890");
        newDetails.setPrice("29.99");
        try {
            newDetails.setPublishDate("01/2023");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String filename = "new_cover_image.jpg";

        when(bookRepository.save(book)).thenReturn(book);

        // Act
        Book updatedBook = updateBookByIdStrategy.update(book, newDetails, filename);

        // Assert
        assertEquals("New Title", updatedBook.getTitle());
        assertEquals("New Author", updatedBook.getAuthor());
        assertEquals("1234567890", updatedBook.getIsbn());
        assertEquals("29.99", updatedBook.getPrice());
        assertEquals("01/2023", updatedBook.getPublishDate());
        assertEquals(filename, updatedBook.getCoverImage());
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    void update_shouldThrowRuntimeExceptionWhenSaveFails() {
        // Arrange
        Book book = new Book();
        book.setId(1);

        Book newDetails = new Book();
        newDetails.setTitle("New Title");
        newDetails.setAuthor("New Author");
        newDetails.setIsbn("1234567890");
        newDetails.setPrice("29.99");
       // newDetails.setPublishDate("2023-01-01");

        when(bookRepository.save(book)).thenThrow(new RuntimeException("Unexpected error"));

        // Act and Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            updateBookByIdStrategy.update(book, newDetails, null);
        });

        assertEquals("An error occurred while updating the book", exception.getMessage());
        verify(bookRepository, times(1)).save(book);
    }
}