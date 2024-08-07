package com.bookcatalog.bookcatalog.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.BookShortDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.service.BookService;

public class BookControllerTest {

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final String UPLOAD_DIR = "/upload/dir/";

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private SecurityContext securityContext;
    private Authentication authentication;
    private User currentUser;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        currentUser = new User();
        when(authentication.getPrincipal()).thenReturn(currentUser);
    }

    @Test
    public void testCreateBook_SuccessWithFile() throws IOException {

        currentUser.setRole(Role.SUPER);

        Book book = new Book();
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[(int) (MAX_FILE_SIZE - 1)]);

        Book savedBook = new Book();
        when(bookService.createBook(book)).thenReturn(savedBook);

        ResponseEntity<?> response = bookController.createBook(book, file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(savedBook, response.getBody());

        verify(bookService, times(1)).createBook(book);
        assertNotNull(book.getCoverImage());
    }

    @Test
    public void testCreateBook_SuccessWithoutFile() throws IOException {

        currentUser.setRole(Role.SUPER);

        Book book = new Book();

        Book savedBook = new Book();
        when(bookService.createBook(book)).thenReturn(savedBook);

        ResponseEntity<?> response = bookController.createBook(book, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(savedBook, response.getBody());

        verify(bookService, times(1)).createBook(book);
        assertNull(book.getCoverImage());
    }

    @Test
    public void testCreateBook_Unauthorized() throws IOException {

        currentUser.setRole(Role.READER);

        Book book = new Book();
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[(int) (MAX_FILE_SIZE - 1)]);

        ResponseEntity<?> response = bookController.createBook(book, file);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You do not have permission to create book. Only SUPER or ADMIN is allowed.", response.getBody());

        verify(bookService, never()).createBook(any(Book.class));
    }

    @Test
    public void testCreateBook_FileSizeAtLimit() throws IOException {

        Book book = new Book();
        MockMultipartFile fileAtLimit = new MockMultipartFile("file", "file.txt", "text/plain", new byte[(int) MAX_FILE_SIZE]);

        User currentUser = new User();
        currentUser.setRole(Role.ADMIN);
        when(authentication.getPrincipal()).thenReturn(currentUser);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        when(bookService.createBook(any(Book.class))).thenReturn(book);

        ResponseEntity<?> response = bookController.createBook(book, fileAtLimit);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(book, response.getBody());

        verify(bookService, times(1)).createBook(any(Book.class));
    }

    @Test
    public void testCreateBook_FileSizeExceedsLimit() throws IOException {
        currentUser.setRole(Role.ADMIN);

        Book book = new Book();
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[(int) (MAX_FILE_SIZE + 1)]);

        ResponseEntity<?> response = bookController.createBook(book, file);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("File size exceeds 2MB size limit", response.getBody());

        verify(bookService, never()).createBook(any(Book.class));
    }

    @Test
    public void testGetBookById_Success() {

        Book book = new Book();
        book.setId(1);
        when(bookService.getBookById(1)).thenReturn(Optional.of(book));

        ResponseEntity<Book> response = bookController.getBookById(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(book, response.getBody());
    }

    @Test
    public void testGetBookById_NotFound() {

        when(bookService.getBookById(1)).thenReturn(Optional.empty());

        ResponseEntity<Book> response = bookController.getBookById(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testGetAllBooks_Success_AsSuperUser() {
        currentUser.setRole(Role.SUPER);
        List<Book> books = List.of(new Book());
        when(bookService.getAllBooks()).thenReturn(ResponseEntity.ok(books));

        ResponseEntity<List<Book>> response = bookController.getAllBooks();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(books, response.getBody());
    }

    @Test
    public void testGetAllBooks_Success_AsAdminUser() {
        currentUser.setRole(Role.ADMIN);
        List<Book> books = List.of(new Book());
        when(bookService.getAllBooks()).thenReturn(ResponseEntity.ok(books));

        ResponseEntity<List<Book>> response = bookController.getAllBooks();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(books, response.getBody());
    }

    @Test
    public void testGetAllBooks_Forbidden_AsNormalUser() {
        currentUser.setRole(Role.READER);

        ResponseEntity<List<Book>> response = bookController.getAllBooks();

        assertNull(response);
    }

    @Test
    public void testGetAllBooksShort_Success_AsSuperUser() {
        currentUser.setRole(Role.SUPER);
        List<BookShortDto> books = Arrays.asList(new BookShortDto());
        when(bookService.getAllBooksShort()).thenReturn(ResponseEntity.ok(books));

        ResponseEntity<List<BookShortDto>> response = bookController.getAllBooksShort();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(books, response.getBody());
    }

    @Test
    public void testGetAllBooksShort_Success_AsAdminUser() {
        currentUser.setRole(Role.ADMIN);
        List<BookShortDto> books = Arrays.asList(new BookShortDto());
        when(bookService.getAllBooksShort()).thenReturn(ResponseEntity.ok(books));

        ResponseEntity<List<BookShortDto>> response = bookController.getAllBooksShort();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(books, response.getBody());
    }

    @Test
    public void testGetAllBooksShort_Forbidden_AsNonAdminUser() {
        currentUser.setRole(Role.READER);

        ResponseEntity<List<BookShortDto>> response = bookController.getAllBooksShort();

        assertNull(response);
    }

    @Test
    public void testGetBooksByUserId_Success() {
        Set<BookShortDto> books = new HashSet<>(Arrays.asList(new BookShortDto()));
        when(bookService.getBooksByUserId(1)).thenReturn(books);

        ResponseEntity<Set<BookShortDto>> response = bookController.getBooksByUserId(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(books, response.getBody());
    }

    @Test
    public void testGetBooksByUserId_NotFound() {
        when(bookService.getBooksByUserId(1)).thenReturn(Collections.emptySet());

        ResponseEntity<Set<BookShortDto>> response = bookController.getBooksByUserId(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testGetBooksByUserUsernameOrEmail_Success() {

        Set<BookShortDto> books = new HashSet<>(Arrays.asList(new BookShortDto()));
        when(bookService.getBooksByUserIdentifier("identifier")).thenReturn(books);

        ResponseEntity<Set<BookShortDto>> response = bookController.getBooksByUserUsernameOrEmail("identifier");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(books, response.getBody());
    }

    @Test
    public void testGetBooksByUserUsernameOrEmail_NoBooksFound() {

        // Arrange
        String identifier = "testUser";

        when(bookService.getBooksByUserIdentifier(identifier)).thenReturn(Collections.emptySet());

        // Act
        ResponseEntity<Set<BookShortDto>> response = bookController.getBooksByUserUsernameOrEmail(identifier);

        // Assert
        assertEquals(ResponseEntity.notFound().build(), response);
    }

    @Test
    public void testUpdateBook_FileIsNull() throws IOException {

        currentUser.setRole(Role.ADMIN);

        Book existingBook = new Book();
        existingBook.setId(1);

        Book updatedBookDetails = new Book();
        updatedBookDetails.setTitle("Updated Title");

        when(bookService.getBookById(1)).thenReturn(Optional.of(existingBook));
        when(bookService.updateBook(eq(1), any(Book.class), isNull())).thenReturn(updatedBookDetails);

        ResponseEntity<?> response = bookController.updateBook(1, updatedBookDetails, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedBookDetails, response.getBody());

        verify(bookService, times(1)).updateBook(eq(1), any(Book.class), isNull());
    }

    @Test
    public void testUpdateBook_FileIsEmpty() throws IOException {

        currentUser.setRole(Role.ADMIN);

        Book existingBook = new Book();
        existingBook.setId(1);

        Book updatedBookDetails = new Book();
        updatedBookDetails.setTitle("Updated Title");

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);

        when(bookService.getBookById(1)).thenReturn(Optional.of(existingBook));
        when(bookService.updateBook(eq(1), any(Book.class), isNull())).thenReturn(updatedBookDetails);

        ResponseEntity<?> response = bookController.updateBook(1, updatedBookDetails, file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedBookDetails, response.getBody());

        verify(bookService, times(1)).updateBook(eq(1), any(Book.class), isNull());
    }

    @Test
    public void testUpdateBook_FileSizeExceedsLimit() throws IOException {
        currentUser.setRole(Role.ADMIN);

        Book existingBook = new Book();
        existingBook.setId(1);

        Book updatedBookDetails = new Book();
        updatedBookDetails.setTitle("Updated Title");

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[(int) (MAX_FILE_SIZE + 1)]);

        when(bookService.getBookById(1)).thenReturn(Optional.of(existingBook));

        ResponseEntity<?> response = bookController.updateBook(1, updatedBookDetails, file);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("File size exceeds 2MB size limit", response.getBody());

        verify(bookService, never()).updateBook(anyInt(), any(Book.class), anyString());
    }

    @Test
    public void testUpdateBook_FileSizeAtLimit_UserAdmin() throws IOException {

        currentUser.setRole(Role.ADMIN);

        Book existingBook = new Book();
        existingBook.setId(1);

        Book updatedBookDetails = new Book();
        updatedBookDetails.setTitle("Updated Title");

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[(int) MAX_FILE_SIZE]);

        when(bookService.getBookById(1)).thenReturn(Optional.of(existingBook));
        when(bookService.updateBook(eq(1), any(Book.class), anyString())).thenReturn(updatedBookDetails);

        ResponseEntity<?> response = bookController.updateBook(1, updatedBookDetails, file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedBookDetails, response.getBody());

        verify(bookService, times(1)).updateBook(eq(1), any(Book.class), anyString());
    }

    @Test
    public void testUpdateBook_FileSizeWithinLimit_UserAdmin() throws IOException {

        currentUser.setRole(Role.ADMIN);

        Book existingBook = new Book();
        existingBook.setId(1);

        Book updatedBookDetails = new Book();
        updatedBookDetails.setTitle("Updated Title");

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[(int) MAX_FILE_SIZE - 1]);

        when(bookService.getBookById(1)).thenReturn(Optional.of(existingBook));
        when(bookService.updateBook(eq(1), any(Book.class), anyString())).thenReturn(updatedBookDetails);

        ResponseEntity<?> response = bookController.updateBook(1, updatedBookDetails, file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedBookDetails, response.getBody());

        verify(bookService, times(1)).updateBook(eq(1), any(Book.class), anyString());
    }

    @Test
    public void testUpdateBook_FileSizeWithinLimit_UserSuper() throws IOException {

        currentUser.setRole(Role.SUPER);

        Book existingBook = new Book();
        existingBook.setId(1);

        Book updatedBookDetails = new Book();
        updatedBookDetails.setTitle("Updated Title");

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[(int) MAX_FILE_SIZE - 1]);

        when(bookService.getBookById(1)).thenReturn(Optional.of(existingBook));
        when(bookService.updateBook(eq(1), any(Book.class), anyString())).thenReturn(updatedBookDetails);

        ResponseEntity<?> response = bookController.updateBook(1, updatedBookDetails, file);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedBookDetails, response.getBody());

        verify(bookService, times(1)).updateBook(eq(1), any(Book.class), anyString());
    }

    @Test
    public void testUpdateBook_BookIsNull() throws IOException {

        // Arrange
        Integer bookId = 1;
        Book bookDetails = new Book();
        MockMultipartFile file = new MockMultipartFile("file", "filename.txt", "text/plain", "some content".getBytes());

        when(bookService.getBookById(bookId)).thenReturn(Optional.empty());

        // Act
        ResponseEntity<?> response = bookController.updateBook(bookId, bookDetails, file);

        // Assert
        assertEquals(ResponseEntity.notFound().build(), response);
        verify(bookService, times(1)).getBookById(bookId);
    }

    @Test
    public void testUpdateBook_UserRoleIsNotSuperOrAdmin_Failure() throws IOException {

        // Arrange
        User currentUser = new User();
        currentUser.setRole(Role.READER);
        when(authentication.getPrincipal()).thenReturn(currentUser);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        Integer bookId = 4;
        Book bookDetails = new Book();

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[1024]);

        when(bookService.getBookById(bookId)).thenReturn(Optional.of(bookDetails));

        // Act
        ResponseEntity<?> response = bookController.updateBook(bookId, bookDetails, file);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You do not have permission to update this book. Only SUPER or ADMIN is allowed.", response.getBody());
        verify(bookService, times(1)).getBookById(bookId);
    }

    @Test
    public void testDeleteBook_UserSuper_Success() {
        currentUser.setRole(Role.SUPER);

        Book existingBook = new Book();
        existingBook.setId(1);

        when(bookService.getBookById(1)).thenReturn(Optional.of(existingBook));
        doNothing().when(bookService).deleteBookById(1);

        ResponseEntity<?> response = bookController.deleteBook(1);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(bookService, times(1)).deleteBookById(1);
    }

    @Test
    public void testDeleteBook_UserAdmin_Success() {
        currentUser.setRole(Role.ADMIN);

        Book existingBook = new Book();
        existingBook.setId(1);

        when(bookService.getBookById(1)).thenReturn(Optional.of(existingBook));
        doNothing().when(bookService).deleteBookById(1);

        ResponseEntity<?> response = bookController.deleteBook(1);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(bookService, times(1)).deleteBookById(1);
    }

    @Test
    public void testDeleteBook_NotFound() {
        currentUser.setRole(Role.SUPER);

        when(bookService.getBookById(1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = bookController.deleteBook(1);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(bookService, never()).deleteBookById(anyInt());
    }

    @Test
    public void testDeleteBook_Unauthorized() {
        currentUser.setRole(Role.READER);

        Book existingBook = new Book();
        existingBook.setId(1);

        when(bookService.getBookById(1)).thenReturn(Optional.of(existingBook));

        ResponseEntity<?> response = bookController.deleteBook(1);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("You do not have permission to update this book. Only SUPER or ADMIN is allowed.", response.getBody());

        verify(bookService, never()).deleteBookById(anyInt());
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
