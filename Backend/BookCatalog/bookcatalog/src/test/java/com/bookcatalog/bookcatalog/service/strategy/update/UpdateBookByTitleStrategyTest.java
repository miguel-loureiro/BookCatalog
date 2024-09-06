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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UpdateBookByTitleStrategyTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private UpdateBookByTitleStrategy updateBookByTitleStrategy;

    private Book existingBook;
    private Book newBookDetails;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);

        // Setup some mock books
        existingBook = new Book();
        existingBook.setId(1);
        existingBook.setTitle("Existing Title");
        existingBook.setAuthor("Existing Author");
        existingBook.setIsbn("1234567890123");
        existingBook.setPrice("19.99");
        existingBook.setPublishDate("01/2020");
        existingBook.setCoverImage("existing_cover.jpg");

        newBookDetails = new Book();
        newBookDetails.setTitle("New Title");
        newBookDetails.setAuthor("New Author");
        newBookDetails.setIsbn("9876543210987");
        newBookDetails.setPrice("24.99");
        newBookDetails.setPublishDate("12/2021");
        newBookDetails.setCoverImage("new_cover.jpg");
    }

    @Test
    void testUpdateBook_Success() throws IOException {
        // Arrange
        when(bookRepository.findBookByTitle(existingBook.getTitle())).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(existingBook)).thenReturn(existingBook);

        // Act
        Book updatedBook = updateBookByTitleStrategy.update(existingBook, newBookDetails, null);

        // Assert
        assertNotNull(updatedBook);
        assertEquals(newBookDetails.getTitle(), updatedBook.getTitle());
        assertEquals(newBookDetails.getAuthor(), updatedBook.getAuthor());
        assertEquals(newBookDetails.getIsbn(), updatedBook.getIsbn());
        assertEquals(newBookDetails.getPrice(), updatedBook.getPrice());
        assertEquals(newBookDetails.getPublishDate(), updatedBook.getPublishDate());
        assertEquals(existingBook.getCoverImage(), updatedBook.getCoverImage());  // Filename is null, so cover image should not change
        verify(bookRepository, times(1)).save(existingBook);
    }

    @Test
    void testUpdateBook_SuccessWithCoverImage() throws IOException {
        // Arrange
        when(bookRepository.findBookByTitle(existingBook.getTitle())).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(existingBook)).thenReturn(existingBook);

        // Act
        Book updatedBook = updateBookByTitleStrategy.update(existingBook, newBookDetails, "updated_cover.jpg");

        // Assert
        assertNotNull(updatedBook);
        assertEquals(newBookDetails.getTitle(), updatedBook.getTitle());
        assertEquals(newBookDetails.getAuthor(), updatedBook.getAuthor());
        assertEquals(newBookDetails.getIsbn(), updatedBook.getIsbn());
        assertEquals(newBookDetails.getPrice(), updatedBook.getPrice());
        assertEquals(newBookDetails.getPublishDate(), updatedBook.getPublishDate());
        assertEquals("updated_cover.jpg", updatedBook.getCoverImage());
        verify(bookRepository, times(1)).save(existingBook);
    }

    @Test
    void testUpdateBook_BookNotFound() {
        // Arrange
        when(bookRepository.findBookByTitle(existingBook.getTitle())).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException thrown = assertThrows(EntityNotFoundException.class, () -> {
            updateBookByTitleStrategy.update(existingBook, newBookDetails, null);
        });

        assertEquals("Book with title Existing Title not found", thrown.getMessage());
        verify(bookRepository, times(1)).findBookByTitle(existingBook.getTitle());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testUpdateBook_SaveFailure() throws IOException {
        // Arrange
        when(bookRepository.findBookByTitle(existingBook.getTitle())).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(existingBook)).thenThrow(new RuntimeException("Database save error"));

        // Act & Assert
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            updateBookByTitleStrategy.update(existingBook, newBookDetails, null);
        });

        assertEquals("An error occurred while updating the book", thrown.getMessage());
        verify(bookRepository, times(1)).save(existingBook);
    }
}