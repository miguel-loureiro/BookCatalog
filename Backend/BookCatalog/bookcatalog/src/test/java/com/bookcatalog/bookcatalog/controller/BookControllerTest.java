package com.bookcatalog.bookcatalog.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.service.BookService;

public class BookControllerTest {


    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    @BeforeEach
    void setUp() throws IOException {

        MockitoAnnotations.openMocks(this);
        Book book = new Book();
        book.setId(1);
        book.setTitle("Test Title");
        book.setAuthor("Test Author");
        book.setPrice("1.00");
        book.setPublishDate("01/1999");
        book.setCoverImage("testfile");
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
        savedBook.setTitle("The book One");
        savedBook.setAuthor("Mike");
        savedBook.setPrice("19.99");
        savedBook.setPublishDate("05/2021");
        savedBook.setCoverImage("testfile.jpg");
        
        when(bookService.createBook(any(Book.class))).thenReturn(savedBook);

        //act
        System.out.println(book.getPublishDate());
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

    @Test
    public void testCreateBookWithLargeJpgFile() throws IOException {

        //arrange
        Book book = new Book();
        book.setTitle("The Book Two");

        byte[] largeFile = new byte[3 * 1024 * 1024]; //3MB file;
        MultipartFile file = new MockMultipartFile(
            "file",
            "largefile.jpg",
            "/uploads/jpeg",
            largeFile);

        //act
        ResponseEntity<?> responseEntity = bookController.createBook(book, file);

        //assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("File size exceeds 2MB size limit", responseEntity.getBody());
    }

    @Test
    public void testCreateBookWithoutFile() throws IOException {

        //arrange
        Book book = new Book();
        book.setTitle("The Book Three");

        Book savedBook = new Book();
        savedBook.setId(3);
        savedBook.setTitle("The Book Three");

        when(bookService.createBook(any(Book.class))).thenReturn(savedBook);

        //act
        ResponseEntity<?> responseEntity = bookController.createBook(book, null);

        //assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("The file is null or empty", responseEntity.getBody());
    }

    @Test
    public void testCreateBookWithEmptyFile() throws IOException {
        // Arrange
        Book book = new Book();
        book.setTitle("Test Book");

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]); // Empty file

        // Act
        ResponseEntity<?> responseEntity = bookController.createBook(book, emptyFile);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("The file is null or empty", responseEntity.getBody());
    }

    @Test
    public void testGetBookById_Success() throws IOException {
        //arrange
        Book book = new Book();
        book.setId(1);
        book.setTitle("Test Title");
        book.setAuthor("Test Author");
        book.setPrice("1.00");
        book.setPublishDate("01/1999");
        book.setCoverImage("testfile");
        when(bookService.getBookById(anyInt())).thenReturn(Optional.of(book));

         //act
        ResponseEntity<Book> responseEntity = bookController.getBookById(1);

        //assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(book, responseEntity.getBody());
    }

    @Test
    public void testGetBookById_NotFound() {

        //arrange
        when(bookService.getBookById(anyInt())).thenReturn(Optional.empty());

         //act
        ResponseEntity<Book> responseEntity = bookController.getBookById(1);

        //assert
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void testGetAllBooks_Success() throws IOException {

        //arrange
        Book book1 = new Book();
        book1.setId(1);
        book1.setTitle("Test Title1");
        book1.setAuthor("Test Author1");
        book1.setPrice("1.00");
        book1.setPublishDate("01/1999");
        book1.setCoverImage("testfile1");

        Book book2 = new Book();
        book2.setId(2);
        book2.setTitle("Test Title2");
        book2.setAuthor("Test Author2");
        book2.setPrice("2.00");
        book2.setPublishDate("02/2000");
        book2.setCoverImage("testfile2");

        List<Book> books = Arrays.asList(book1, book2);
        
        //arrange
        when(bookService.getAllBooks()).thenReturn(books);

        List<Book> result = bookController.getAllBooks();

        //assert
        assertEquals(2, result.size());
        assertEquals(book1, result.get(0));
        assertEquals(book2, result.get(1));
    }

    @Test
    void testUpdateBook_WithFile() throws Exception {

        Book book = new Book();
        book.setId(1);
        book.setTitle("Test Title");
        book.setAuthor("Test Author");
        book.setPrice("1.00");
        book.setPublishDate("01/1999");
        book.setCoverImage("testfile");

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.IMAGE_JPEG_VALUE, "Test file content".getBytes());
        Book updatedBook = new Book();
        updatedBook.setId(1);
        updatedBook.setTitle("Updated Title");
        updatedBook.setAuthor("Updated Author");
        updatedBook.setPrice("150.0");
        updatedBook.setCoverImage("new_image.jpg");

        when(bookService.updateBook(eq(1), any(Book.class), anyString())).thenReturn(updatedBook);

        ResponseEntity<?> response = bookController.updateBook(1, book, file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedBook, response.getBody());
        verify(bookService, times(1)).updateBook(eq(1), any(Book.class), anyString());
    }

    @Test
    public void testUpdateBookWithEmptyFile_Failure() throws IOException {

        //arrange
        Book initialBook = new Book();
        initialBook.setId(1);
        initialBook.setTitle("The book One");
        initialBook.setAuthor("Mike");
        initialBook.setPrice("19.99");
        initialBook.setPublishDate("05/2021");
        initialBook.setCoverImage("testfile.jpg");

        Book updatedBook = new Book();
        updatedBook.setId(1);
        updatedBook.setTitle("Updated Test Title");
        updatedBook.setAuthor("Updated Test Author");
        updatedBook.setPrice("11.00");
        updatedBook.setPublishDate("12/2002");
        updatedBook.setCoverImage("updatedtestfile");

        when(bookService.updateBook(anyInt(), any(Book.class), anyString())).thenReturn(updatedBook);

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]); // Empty file

         //act
        ResponseEntity<?> responseEntity = bookController.updateBook(1, updatedBook, emptyFile);

        //assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("The file is empty", responseEntity.getBody());
    }

    @Test
    public void testUpdateBookWithLargeFile_Failure() throws IOException {

        //arrange
        Book initialBook = new Book();
        initialBook.setId(1);
        initialBook.setTitle("The book One");
        initialBook.setAuthor("Mike");
        initialBook.setPrice("19.99");
        initialBook.setPublishDate("05/2021");
        initialBook.setCoverImage("testfile.jpg");

        Book updatedBook = new Book();
        updatedBook.setId(1);
        updatedBook.setTitle("Updated Test Title");
        updatedBook.setAuthor("Updated Test Author");
        updatedBook.setPrice("11.00");
        updatedBook.setPublishDate("12/2002");
        updatedBook.setCoverImage("updatedtestfile");

        when(bookService.updateBook(anyInt(), any(Book.class), anyString())).thenReturn(updatedBook);

        byte[] largeFile = new byte[3 * 1024 * 1024]; //3MB file;
        MultipartFile file = new MockMultipartFile(
            "file",
            "largefile.jpg",
            "/uploads/jpeg",
            largeFile);

         //act
        ResponseEntity<?> responseEntity = bookController.updateBook(1, updatedBook, file);

        //assert
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals("File size exceeds 2MB size limit", responseEntity.getBody());
    }

    @Test
    public void testDeleteBookById_Success() throws IOException {

        //arrange
        Book book1 = new Book();
        book1.setId(1);
        book1.setTitle("Test Title1");
        book1.setAuthor("Test Author1");
        book1.setPrice("1.00");
        book1.setPublishDate("01/1999");
        book1.setCoverImage("testfile1");

         //act
        ResponseEntity<?> responseEntity = bookController.deleteBookById(1);

        //assert
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
    }
}
