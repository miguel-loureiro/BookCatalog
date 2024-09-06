package com.bookcatalog.bookcatalog.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.*;

import com.bookcatalog.bookcatalog.exceptions.BookNotFoundException;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.repository.BookRepository;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import com.bookcatalog.bookcatalog.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.service.BookService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

public class BookControllerTest {

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final String UPLOAD_DIR = "/upload/dir/";

    @Mock
    private BookService bookService;

    @Mock
    private UserService userService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

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

    private void mockCurrentUser(User user) {

        if (user == null) {

            SecurityContextHolder.clearContext();
            when(securityContext.getAuthentication()).thenReturn(null);
        } else {

            UserDetails userDetails = mock(UserDetails.class);
            when(userDetails.getUsername()).thenReturn(user.getUsername());

            SecurityContextHolder.setContext(securityContext);

            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(securityContext.getAuthentication()).thenReturn(authentication);

            when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        }
    }

    @Test
    public void testGetBook_Success() throws Exception {
        // Arrange
        Book foundBook = new Book("Title", "Author");

        when(bookService.getBook(anyString(), anyString())).thenReturn(foundBook);

        // Act
        ResponseEntity<?> response = bookController.getBook("title", "Title");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(foundBook, response.getBody());
    }

    @Test
    public void testGetBook_NotFound_Failure() {
        // Arrange
        when(bookService.getBook(anyString(), anyString())).thenThrow(new BookNotFoundException("Book not found", null));

        // Act
        ResponseEntity<?> response = bookController.getBook("title", "Title");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Book not found", response.getBody());
    }

    @Test
    public void testGetAllBooks_Success_AsSuperUser() throws IOException {
        // Arrange
        currentUser.setRole(Role.SUPER);
        Page<Book> booksPage = new PageImpl<>(List.of(new Book()));
        when(bookService.getAllBooks(0, 10)).thenReturn(ResponseEntity.ok(booksPage));

        // Act
        ResponseEntity<Page<Book>> response = bookController.getAllBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(booksPage, response.getBody());
    }

    @Test
    public void testGetAllBooks_Success_AsAdminUser() throws IOException {
        // Arrange
        currentUser.setRole(Role.ADMIN);
        Page<Book> booksPage = new PageImpl<>(List.of(new Book()));
        when(bookService.getAllBooks(0, 10)).thenReturn(ResponseEntity.ok(booksPage));

        // Act
        ResponseEntity<Page<Book>> response = bookController.getAllBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(booksPage, response.getBody());
    }

    @Test
    public void testGetAllBooks_Exception_AsAdminUser() throws IOException {
        // Arrange
        currentUser.setRole(Role.ADMIN);
        when(bookService.getAllBooks(0, 10)).thenThrow(new RuntimeException("An error occurred while fetching books"));

        // Act
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookController.getAllBooks(0, 10);
        });

        // Assert
        assertEquals("An error occurred while fetching books", exception.getMessage());
    }

    @Test
    public void testGetAllBooks_Forbidden_AsNormalUser() throws IOException {
        // Arrange
        currentUser.setRole(Role.READER);

        // Act
        ResponseEntity<Page<Book>> response = bookController.getAllBooks(0, 10);

        // Assert
        assertNull(response);
    }

    @Test
    public void testGetBooksByUserId_Success() {

        List<Book> bookList = List.of(new Book("Title1", "Author1"), new Book("Title2", "Author2"));
        Page<Book> booksPage = new PageImpl<>(bookList, PageRequest.of(0, 10), bookList.size());

        when(bookService.getBooksByUserId(1, 0, 10)).thenReturn(booksPage);

        // Act
        ResponseEntity<Page<Book>> response = bookController.getBooksByUserId(1, 0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(booksPage, response.getBody());
    }

    @Test
    public void testGetBooksByUserId_NotFound() {

        when(bookService.getBooksByUserId(1, 0, 10)).thenReturn(Page.empty());

        ResponseEntity<Page<Book>> response = bookController.getBooksByUserId(1, 0, 10);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testGetBooksByUserUsernameOrEmail_Success() {

        List<Book> bookList = List.of(new Book("Title1", "Author1"), new Book("Title2", "Author2"));
        Page<Book> booksPage = new PageImpl<>(bookList, PageRequest.of(0, 10), bookList.size());
        when(bookService.getBooksByUserIdentifier("identifier", 0, 10)).thenReturn(booksPage);

        ResponseEntity<Page<Book>> response = bookController.getBooksByUserIdentifier("identifier", 0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(booksPage, response.getBody());
    }

    @Test
    public void testGetBooksByUserUsernameOrEmail_NoBooksFound() {

        // Arrange
        String identifier = "testUser";
        when(bookService.getBooksByUserIdentifier(identifier, 0, 10)).thenReturn(Page.empty());

        // Act
        ResponseEntity<Page<Book>> response = bookController.getBooksByUserIdentifier(identifier, 0, 10);

        // Assert
        assertEquals(ResponseEntity.notFound().build(), response);
    }

    @Test
    public void testCreateBook_SuccessWithFile() throws IOException {

        currentUser.setRole(Role.SUPER);
        mockCurrentUser(currentUser);

        Book book = new Book();
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[(int) (MAX_FILE_SIZE - 1)]);

        Book savedBook = new Book();
        savedBook.setCoverImage("timestamp_test.jpg");

        when(bookService.createBook(any(Book.class), any(MultipartFile.class))).thenReturn(ResponseEntity.ok(savedBook));

        // Act
        ResponseEntity<?> response = bookController.createBook(book, file);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(savedBook, response.getBody());
        verify(bookService, times(1)).createBook(book, file);
    }

    @Test
    public void testCreateBook_SuccessWithoutFile() throws IOException {
        // Arrange
        mockCurrentUser(currentUser);

        Book book = new Book();
        Book savedBook = new Book();

        when(bookService.createBook(any(Book.class), isNull())).thenReturn(ResponseEntity.ok(savedBook));

        // Act
        ResponseEntity<?> response = bookController.createBook(book, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(savedBook, response.getBody());
        verify(bookService, times(1)).createBook(book, null);
    }

    @Test
    public void testCreateBook_FileSizeExceedsLimit() throws IOException {
        // Arrange
        mockCurrentUser(currentUser);

        Book book = new Book();
        MockMultipartFile file = new MockMultipartFile("file", "largefile.jpg", "image/jpeg", new byte[3 * 1024 * 1024]); // 3MB file

        when(bookService.createBook(any(Book.class), any(MultipartFile.class)))
                .thenReturn(ResponseEntity.badRequest().build());

        // Act
        ResponseEntity<?> response = bookController.createBook(book, file);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(bookService, times(1)).createBook(book, file);
    }

    @Test
    public void testCreateBook_UnauthorizedUser() throws IOException {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(Optional.empty());

        Book book = new Book();
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[1024]);

        when(bookService.createBook(any(Book.class), any(MultipartFile.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());

        // Act
        ResponseEntity<?> response = bookController.createBook(book, file);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(bookService, times(1)).createBook(book, file);
    }

    @Test
    public void testCreateBook_ForbiddenUser() throws IOException {
        // Arrange
        currentUser.setRole(Role.GUEST);
        mockCurrentUser(currentUser);

        Book book = new Book();
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[1024]);

        when(bookService.createBook(any(Book.class), any(MultipartFile.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
        // Act
        ResponseEntity<?> response = bookController.createBook(book, file);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(bookService, times(1)).createBook(book, file);
    }

    @Test
    public void testUpdateBook_SuccessWithFile() throws IOException {
        // Arrange
        String type = "id";
        String identifier = "1";
        Book bookDetails = new Book();  // Populate with necessary data
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{1, 2, 3, 4});

        when(bookService.updateBook(anyString(), anyString(), any(Book.class), anyString()))
                .thenReturn(ResponseEntity.ok().build());

        // Act
        ResponseEntity<?> response = bookController.updateBook(type, identifier, bookDetails, file);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ArgumentCaptor<String> filenameCaptor = ArgumentCaptor.forClass(String.class);
        verify(bookService, times(1)).updateBook(eq(identifier), eq(type), eq(bookDetails), filenameCaptor.capture());

        String capturedFilename = filenameCaptor.getValue();
        assert capturedFilename != null;
        assert capturedFilename.matches("\\d+_test\\.jpg");
    }

    @Test
    public void testUpdateBook_SuccessWithoutFile() throws IOException {
        // Arrange
        String identifier = "1";
        String type = "id";
        Book bookDetails = new Book();
        Book updatedBook = new Book();

        when(bookService.updateBook(identifier, type, bookDetails, null)).thenReturn(ResponseEntity.ok(updatedBook));

        // Act
        ResponseEntity<?> response = bookController.updateBook(type, identifier, bookDetails, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(bookService, times(1)).updateBook(identifier, type, bookDetails, null);
    }

    @Test
    public void testUpdateBook_FileSizeExceedsLimit() throws IOException {
        // Arrange
        String identifier = "1";
        String type = "id";
        Book bookDetails = new Book();
        MockMultipartFile file = new MockMultipartFile("file", "largefile.jpg", "image/jpeg", new byte[3 * 1024 * 1024]); // 3MB file

        // Act
        ResponseEntity<?> response = bookController.updateBook(type, identifier, bookDetails, file);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("File size exceeds 2MB size limit", response.getBody());
        verify(bookService, never()).updateBook(anyString(), anyString(), any(Book.class), anyString());
    }

    @Test
    public void testUpdateBook_BookNotFound() throws IOException {
        // Arrange
        String type = "id";
        String identifier = "1";
        MultipartFile file = null;

        // Act
        ResponseEntity<?> response = bookController.updateBook(type, identifier, null, file);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("The 'book' part is required.", response.getBody());
        verify(bookService, never()).updateBook(anyString(), anyString(), any(Book.class), anyString());
    }

    @Test
    public void testDeleteBook_Success() {
        // Arrange
        String identifier = "1";
        String type = "id";

        when(bookService.deleteBook(identifier, type)).thenReturn(ResponseEntity.ok().build());

        // Act
        ResponseEntity<?> response = bookController.deleteBook(type, identifier);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(bookService, times(1)).deleteBook(identifier, type);
    }

    @Test
    public void testDeleteBook_BookNotFound() {
        // Arrange
        String identifier = "1";
        String type = "id";

        when(bookService.deleteBook(identifier, type)).thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        // Act
        ResponseEntity<?> response = bookController.deleteBook(type, identifier);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(bookService, times(1)).deleteBook(identifier, type);
    }

    @Test
    public void testDeleteBook_Forbidden() {
        // Arrange
        String identifier = "1";
        String type = "id";

        when(bookService.deleteBook(identifier, type)).thenReturn(ResponseEntity.status(HttpStatus.FORBIDDEN).build());

        // Act
        ResponseEntity<?> response = bookController.deleteBook(type, identifier);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(bookService, times(1)).deleteBook(identifier, type);
    }

    @Test
    public void testAddBookToCurrentUser_Success() {
        // Arrange
        String identifier = "1";
        String type = "id";

        when(bookService.addBookToCurrentUser(identifier, type)).thenReturn(ResponseEntity.ok().build());

        // Act
        ResponseEntity<?> response = bookController.addBookToCurrentUser(type, identifier);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(bookService, times(1)).addBookToCurrentUser(identifier, type);
    }

    @Test
    public void testAddBookToCurrentUser_BookNotFound() {
        // Arrange
        String identifier = "1";
        String type = "id";

        when(bookService.addBookToCurrentUser(identifier, type)).thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found"));

        // Act
        ResponseEntity<?> response = bookController.addBookToCurrentUser(type, identifier);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Book not found", response.getBody());
        verify(bookService, times(1)).addBookToCurrentUser(identifier, type);
    }

    @Test
    public void testDeleteBookFromCurrentUser_Success() {
        // Arrange
        String identifier = "1";
        String type = "id";

        when(bookService.deleteBookFromCurrentUser(identifier, type)).thenReturn(ResponseEntity.ok().build());

        // Act
        ResponseEntity<?> response = bookController.deleteBookFromCurrentUser(type, identifier);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(bookService, times(1)).deleteBookFromCurrentUser(identifier, type);
    }

    @Test
    public void testDeleteBookFromCurrentUser_BookNotFound() {
        // Arrange
        String identifier = "1";
        String type = "id";

        when(bookService.deleteBookFromCurrentUser(identifier, type)).thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).body("Book not found"));

        // Act
        ResponseEntity<?> response = bookController.deleteBookFromCurrentUser(type, identifier);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Book not found", response.getBody());
        verify(bookService, times(1)).deleteBookFromCurrentUser(identifier, type);
    }

    @Test
    public void testSaveAllBooks_Success() {
        // Arrange
        List<Book> books = List.of(new Book(), new Book());

        // Act
        ResponseEntity<?> response = bookController.saveAllBooks(books);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(bookService, times(1)).saveAll(books);
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
