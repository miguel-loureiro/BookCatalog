package com.bookcatalog.bookcatalog.controller;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class GuestControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private GuestController guestController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAvailableBooks_ReturnsBooksPage() throws IOException {
        // Arrange
        List<Book> books = new ArrayList<>();
        books.add(new Book(1, "Book 1", "Author 1"));
        books.add(new Book(2, "Book 2", "Author 2"));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());
        ResponseEntity<Page<Book>> responseEntity = ResponseEntity.ok(bookPage);

        when(bookService.getAllBooks(0, 10)).thenReturn(responseEntity);

        // Act
        ResponseEntity<Page<Book>> response = guestController.getAvailableBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotalElements());
        assertEquals(bookPage, response.getBody());
    }

    @Test
    public void testGetAvailableBooks_ReturnsEmptyPageWhenNoBooksAvailable() throws IOException {
        // Arrange
        List<Book> books = new ArrayList<>();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Book> emptyPage = new PageImpl<>(books, pageable, 0);
        ResponseEntity<Page<Book>> responseEntity = ResponseEntity.ok(emptyPage);

        when(bookService.getAllBooks(0, 10)).thenReturn(responseEntity);

        // Act
        ResponseEntity<Page<Book>> response = guestController.getAvailableBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().getTotalElements());
    }
}