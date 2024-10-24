package com.bookcatalog.backend.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.*;

import com.bookcatalog.backend.exceptions.BookNotFoundException;
import com.bookcatalog.backend.model.Role;
import com.bookcatalog.backend.model.User;
import com.bookcatalog.backend.model.dto.BookDetailWithoutUserListDto;
import com.bookcatalog.backend.model.dto.BookDto;
import com.bookcatalog.backend.repository.BookRepository;
import com.bookcatalog.backend.repository.UserRepository;
import com.bookcatalog.backend.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bookcatalog.backend.model.Book;
import com.bookcatalog.backend.service.BookService;
import org.springframework.security.core.userdetails.UserDetails;

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
    public void testGetBook_Success() {
        // Arrange
        String identifier = "1";
        String type = "id";
        BookDto bookDto = new BookDto(new Book());

        when(bookService.getBookWithShortUserDetails(identifier, type)).thenReturn(bookDto);

        // Act
        ResponseEntity<?> response = bookController.getBook(type, identifier);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(bookDto, response.getBody());
        verify(bookService, times(1)).getBookWithShortUserDetails(identifier, type);
    }

    @Test
    public void testGetBook_NotFound() {
        // Arrange
        String identifier = "999";
        String type = "id";

        when(bookService.getBookWithShortUserDetails(identifier, type)).thenThrow(new BookNotFoundException("Book not found", null));

        // Act
        ResponseEntity<?> response = bookController.getBook(type, identifier);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Book not found", response.getBody());
    }

    @Test
    public void testGetAllBooks_Success_AsSuperUser() throws IOException {
        // Arrange
        User currentUser = new User();
        currentUser.setRole(Role.SUPER);
        mockCurrentUser(currentUser);

        Page<BookDto> booksPage = new PageImpl<>(List.of(new BookDto(new Book())));
        when(bookService.getAllBooks(0, 10)).thenReturn(ResponseEntity.ok(booksPage));

        // Act
        ResponseEntity<Page<BookDto>> response = bookController.getAllBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(booksPage, response.getBody());
    }

    @Test
    public void testGetBooksOnly_Success() {
        // Arrange
        Set<BookDetailWithoutUserListDto> books = Set.of(new BookDetailWithoutUserListDto(new Book()));
        when(bookService.getOnlyBooks(0, 10)).thenReturn(ResponseEntity.ok(books));

        // Act
        ResponseEntity<Set<BookDetailWithoutUserListDto>> response = bookController.getBooksOnly(0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(books, response.getBody());
    }

    @Test
    public void testGetBooksByUserId_Success() {
        // Arrange
        Page<BookDetailWithoutUserListDto> booksPage = new PageImpl<>(List.of(new BookDetailWithoutUserListDto(new Book())));
        when(bookService.getBooksByUserId(1, 0, 10)).thenReturn(booksPage);

        // Act
        ResponseEntity<Page<BookDetailWithoutUserListDto>> response = bookController.getBooksByUserId(1, 0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(booksPage, response.getBody());
    }

    @Test
    public void testGetBooksByUserId_NotFound() {
        // Arrange
        when(bookService.getBooksByUserId(1, 0, 10)).thenReturn(Page.empty());

        // Act
        ResponseEntity<Page<BookDetailWithoutUserListDto>> response = bookController.getBooksByUserId(1, 0, 10);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetBooksByUserIdentifier_Success() {
        // Arrange
        String identifier = "testuser";
        Page<BookDetailWithoutUserListDto> booksPage = new PageImpl<>(List.of(new BookDetailWithoutUserListDto(new Book())));
        when(bookService.getBooksByUserIdentifier(identifier, 0, 10)).thenReturn(booksPage);

        // Act
        ResponseEntity<Page<BookDetailWithoutUserListDto>> response = bookController.getBooksByUserIdentifier(identifier, 0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(booksPage, response.getBody());
    }

    @Test
    public void testGetBooksByUserIdentifier_NotFound() {
        // Arrange
        String identifier = "testuser";
        when(bookService.getBooksByUserIdentifier(identifier, 0, 10)).thenReturn(Page.empty());

        // Act
        ResponseEntity<Page<BookDetailWithoutUserListDto>> response = bookController.getBooksByUserIdentifier(identifier, 0, 10);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testCreateBook_Success() throws IOException {
        User currentUser = new User();
        currentUser.setRole(Role.ADMIN);
        mockCurrentUser(currentUser);
        // Arrange
        BookDetailWithoutUserListDto bookDto = new BookDetailWithoutUserListDto(new Book());
        when(bookService.createBook(bookDto)).thenReturn(ResponseEntity.ok(new Book()));

        // Act
        ResponseEntity<?> response = bookController.createBook(bookDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(bookService, times(1)).createBook(bookDto);
    }

    @Test
    public void testCreateBook_Unauthorized() throws IOException {
        // Arrange
        BookDetailWithoutUserListDto bookDto = new BookDetailWithoutUserListDto(new Book());
        when(bookService.createBook(bookDto)).thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());

        // Act
        ResponseEntity<?> response = bookController.createBook(bookDto);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(bookService, times(1)).createBook(bookDto);
    }

    @Test
    public void testUpdateBook_Success() throws IOException {
        // Arrange
        Book book = new Book();
        when(bookService.updateBook("1", "id", book)).thenReturn(ResponseEntity.ok(book));

        // Act
        ResponseEntity<?> response = bookController.updateBook("id", "1", book);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(bookService, times(1)).updateBook("1", "id", book);
    }

    @Test
    public void testUpdateBook_BadRequest() throws IOException {
        // Act
        ResponseEntity<?> response = bookController.updateBook("id", "1", null);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("The 'book' part is required.", response.getBody());
    }

    @Test
    public void testDeleteBook_Success() {
        // Arrange
        when(bookService.deleteBook("1", "id")).thenReturn(ResponseEntity.ok().build());

        // Act
        ResponseEntity<Void> response = bookController.deleteBook("id", "1");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(bookService, times(1)).deleteBook("1", "id");
    }

    @Test
    public void testAddBookToCurrentUser_Success() {
        // Arrange
        when(bookService.addBookToCurrentUser("1", "id")).thenReturn(ResponseEntity.ok().build());

        // Act
        ResponseEntity<?> response = bookController.addBookToCurrentUser("id", "1");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(bookService, times(1)).addBookToCurrentUser("1", "id");
    }

    @Test
    public void testDeleteBookFromCurrentUser_Success() {
        // Arrange
        when(bookService.deleteBookFromCurrentUser("1", "id")).thenReturn(ResponseEntity.ok().build());

        // Act
        ResponseEntity<?> response = bookController.deleteBookFromCurrentUser("id", "1");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(bookService, times(1)).deleteBookFromCurrentUser("1", "id");
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }
}