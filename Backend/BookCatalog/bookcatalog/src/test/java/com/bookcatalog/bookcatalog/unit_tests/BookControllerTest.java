package com.bookcatalog.bookcatalog.unit_tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.bookcatalog.bookcatalog.controller.BookController;
import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.service.BookService;

public class BookControllerTest {


    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
    }

    @SuppressWarnings("null")
    @Test
    public void testCreateBookWithValidJpgFile() throws IOException {

        //arrange
        Book book = new Book();
        book.setTitle("The Book One");

        MultipartFile file = new MockMultipartFile(
            "file",
            "testfile.jpg",
            "/uploads/jpeg",
            "Test file".getBytes());

        Book savedBook = new Book();
        savedBook.setId(1);
        savedBook.setTitle("The book one");
        savedBook.setAuthor("Mike");
        savedBook.setPrice("19.99");
        savedBook.setPublishDate("05/2021");
        savedBook.setCoverImage("testfile.jpg");
        
        when(bookService.createBook(any(Book.class))).thenReturn(savedBook);

        //act
        ResponseEntity<?> responseEntity = bookController.createBook(book, file);

        //assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Book responseBody = (Book) responseEntity.getBody();
        assertEquals(savedBook.getId(), responseBody.getId());
        assertEquals(savedBook.getTitle(), responseBody.getTitle());
        assertEquals(savedBook.getAuthor(), responseBody.getAuthor());
        assertEquals(savedBook.getPrice(), responseBody.getPrice());
        assertEquals(savedBook.getPublishDate(), responseBody.getPublishDate());
        assertEquals(savedBook.getCoverImage(), responseBody.getCoverImage());

        //clean up
        Path filepath = Paths.get(System.getProperty("user.dir") + "/uploads", file.getOriginalFilename());
        Files.deleteIfExists(filepath);
    }
}
