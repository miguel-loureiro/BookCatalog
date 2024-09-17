package com.bookcatalog.bookcatalog.service;

import com.bookcatalog.bookcatalog.exceptions.BookNotFoundException;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.BookDetailWithoutUserListDto;
import com.bookcatalog.bookcatalog.model.dto.BookDto;
import com.bookcatalog.bookcatalog.model.dto.UserDto;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.repository.BookRepository;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BookService bookService;

    private User currentUser;
    private Book mockBook;
    private BookDetailWithoutUserListDto mockBookDto;
    private static final int MAX_RETRY_ATTEMPTS = 3;

    @BeforeEach
    public void setUp() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);

        // Set up mock user
        currentUser = new User();
        currentUser.setUsername("currentUser");
        currentUser.setRole(Role.ADMIN);

        // Set up mock book and DTO
        mockBook = new Book();
        mockBook.setTitle("Test Title");
        mockBook.setAuthor("Test Author");

        mockBookDto = new BookDetailWithoutUserListDto(
                "Test Title", "Test Author", "12345", "10.00", "01/2021", "imageUrl"
        );
    }

    private void mockCurrentUser(User user) {
        if (user == null) {
            when(userService.getCurrentUser()).thenReturn(Optional.empty());
        } else {
            when(userService.getCurrentUser()).thenReturn(Optional.of(user));
        }
    }

    private void mockSaveBook() {
        when(bookRepository.save(any(Book.class))).thenReturn(mockBook);
    }

    @Test
    public void testCreateBook_Success() {
        // Arrange
        mockCurrentUser(currentUser);
        mockSaveBook();

        // Act
        ResponseEntity<Book> response = bookService.createBook(mockBookDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockBook.getTitle(), response.getBody().getTitle());
        assertEquals(mockBook.getAuthor(), response.getBody().getAuthor());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    public void testCreateBook_Unauthorized() {
        // Arrange
        mockCurrentUser(null);

        // Act
        ResponseEntity<Book> response = bookService.createBook(mockBookDto);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    public void testCreateBook_Forbidden() {
        // Arrange
        currentUser.setRole(Role.READER); // Reader doesn't have permission
        mockCurrentUser(currentUser);

        // Act
        ResponseEntity<Book> response = bookService.createBook(mockBookDto);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    public void testCreateBook_InternalServerError() {
        // Arrange
        mockCurrentUser(currentUser);
        when(bookRepository.save(any(Book.class))).thenThrow(new RuntimeException("DB Error"));

        // Act
        ResponseEntity<Book> response = bookService.createBook(mockBookDto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    public void testCreateBook_WithBookAsArgument_Success() {
        // Arrange
        when(bookRepository.save(any(Book.class))).thenReturn(mockBook);

        // Act
        Book result = bookService.createBook(mockBook);

        // Assert
        assertNotNull(result);
        assertEquals(mockBook.getTitle(), result.getTitle());
        verify(bookRepository, times(1)).save(mockBook);
    }

    @Test
    public void testCreateBook_BookRepositoryNull_ThrowsIllegalStateException() {
        // Arrange

        BookService bookServiceWithNullRepo = new BookService(null, null, null);

        // Act & Assert
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            bookServiceWithNullRepo.createBook(mockBook);
        });
        assertEquals("BookRepository is not initialized", thrown.getMessage());
    }

    @Test
    public void testGetBookByIdentifier_Id_Success() {
        // Arrange
        mockCurrentUser(currentUser);
        when(bookRepository.getReferenceById(anyInt())).thenReturn(mockBook);

        // Act
        Book result = bookService.getBookByIdentifier("1", "id");

        // Assert
        assertNotNull(result);
        assertEquals(mockBook.getTitle(), result.getTitle());
        verify(bookRepository, times(1)).getReferenceById(1);
    }

    @Test
    public void testGetBookByIdentifier_Title_Success() {
        // Arrange
        mockCurrentUser(currentUser);
        when(bookRepository.findBookByTitle(anyString())).thenReturn(Optional.of(mockBook));

        // Act
        Book result = bookService.getBookByIdentifier("Test Title", "title");

        // Assert
        assertNotNull(result);
        assertEquals(mockBook.getTitle(), result.getTitle());
        verify(bookRepository, times(1)).findBookByTitle("Test Title");
    }

    @Test
    public void testGetBookByIdentifier_ISBN_Success() {
        // Arrange
        mockCurrentUser(currentUser);
        when(bookRepository.findBookByIsbn(anyString())).thenReturn(Optional.of(mockBook));

        // Act
        Book result = bookService.getBookByIdentifier("12345", "isbn");

        // Assert
        assertNotNull(result);
        assertEquals(mockBook.getTitle(), result.getTitle());
        verify(bookRepository, times(1)).findBookByIsbn("12345");
    }

    @Test
    public void testGetBookByIdentifier_Id_BookNotFound_ThrowsBookNotFoundException() {
        // Arrange
        mockCurrentUser(currentUser);
        when(bookRepository.getReferenceById(anyInt())).thenThrow(EntityNotFoundException.class);

        // Act & Assert
        BookNotFoundException thrown = assertThrows(BookNotFoundException.class, () -> {
            bookService.getBookByIdentifier("1", "id");
        });
        assertEquals("Book not found with id: 1", thrown.getMessage());
    }

    @Test
    public void testGetBookByIdentifier_Title_BookNotFound_ThrowsBookNotFoundException() {
        // Arrange
        mockCurrentUser(currentUser);
        when(bookRepository.findBookByTitle(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        BookNotFoundException thrown = assertThrows(BookNotFoundException.class, () -> {
            bookService.getBookByIdentifier("Nonexistent Title", "title");
        });
        assertEquals("Book not found with title: Nonexistent Title", thrown.getMessage());
    }

    @Test
    public void testGetBookByIdentifier_ISBN_BookNotFound_ThrowsBookNotFoundException() {
        // Arrange
        mockCurrentUser(currentUser);
        when(bookRepository.findBookByIsbn(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        BookNotFoundException thrown = assertThrows(BookNotFoundException.class, () -> {
            bookService.getBookByIdentifier("98765", "isbn");
        });
        assertEquals("Book not found with ISBN: 98765", thrown.getMessage());
    }

    @Test
    public void testGetBookByIdentifier_InvalidType_ThrowsIllegalArgumentException() {
        // Arrange
        mockCurrentUser(currentUser);

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            bookService.getBookByIdentifier("1", "invalidType");
        });
        assertEquals("Invalid identifier type: invalidType", thrown.getMessage());
    }

    @Test
    public void testGetBookByIdentifier_CurrentUserNotFound_ThrowsIllegalStateException() {
        // Arrange
        mockCurrentUser(null);  // No current user

        // Act & Assert
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            bookService.getBookByIdentifier("1", "id");
        });
        assertEquals("Current user not found", thrown.getMessage());
    }

    @Test
    public void testGetBookByIdentifier_IdSuccess() {
        // Mock current user and book repository behavior
        mockCurrentUser(currentUser);
        when(bookRepository.getReferenceById(anyInt())).thenReturn(mockBook);

        // Call the method
        Book book = bookService.getBookByIdentifier("1", "id");

        // Assert
        assertNotNull(book);
        assertEquals(mockBook.getTitle(), book.getTitle());
        verify(bookRepository, times(1)).getReferenceById(anyInt());
    }

    @Test
    public void testGetBookByIdentifier_IdNotFound() {
        // Mock current user and book repository behavior
        mockCurrentUser(currentUser);
        when(bookRepository.getReferenceById(anyInt())).thenThrow(new EntityNotFoundException());

        // Assert exception
        assertThrows(BookNotFoundException.class, () -> bookService.getBookByIdentifier("1", "id"));
        verify(bookRepository, times(1)).getReferenceById(anyInt());
    }

    @Test
    public void testGetBookByIdentifier_InvalidType() {
        // Mock current user
        mockCurrentUser(currentUser);

        // Assert exception for invalid identifier type
        assertThrows(IllegalArgumentException.class, () -> bookService.getBookByIdentifier("someId", "invalidType"));
    }

    @Test
    public void testGetAllBooks_Success() throws IOException {
        // Arrange
        User currentUser = new User();
        currentUser.setUsername("currentUser");
        currentUser.setRole(Role.ADMIN);

        mockCurrentUser(currentUser);

        List<Book> books = Arrays.asList(
                new Book("Book 1", "Author 1", "12345", "10.00", new Date(), "url1"),
                new Book("Book 2", "Author 2", "67890", "15.00", new Date(), "url2")
        );

        Page<Book> booksPage = new PageImpl<>(books);
        Pageable pageable = PageRequest.of(0, 10, Sort.by("title").ascending());

        when(bookRepository.findAll(pageable)).thenReturn(booksPage);

        // Act
        ResponseEntity<Page<BookDto>> response = bookService.getAllBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getTotalElements());
        verify(bookRepository, times(1)).findAll(pageable);
    }

    @Test
    public void testGetAllBooks_Forbidden() throws IOException {
        // Mock current user without permission
        currentUser.setRole(Role.GUEST);
        mockCurrentUser(currentUser);

        // Call the method
        ResponseEntity<Page<BookDto>> response = bookService.getAllBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(bookRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    public void testGetAllBooks_Unauthorized() throws IOException {
        // Mock no current user
        mockCurrentUser(null);

        // Call the method
        ResponseEntity<Page<BookDto>> response = bookService.getAllBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(bookRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    public void testGetBookWithShortUserDetails_Success() {
        // Arrange
        User currentUser = new User();
        currentUser.setUsername("currentUser");
        currentUser.setRole(Role.ADMIN);

        mockCurrentUser(currentUser);

        Book book = new Book("Book Title", "Author", "12345", "10.00", new Date(), "url1");
        when(bookRepository.findBookByIsbn("12345")).thenReturn(Optional.of(book));

        // Act
        BookDto result = bookService.getBookWithShortUserDetails("12345", "isbn");

        // Assert
        assertNotNull(result);
        assertEquals("Book Title", result.getTitle());
        assertEquals("12345", result.getIsbn());
        verify(bookRepository, times(1)).findBookByIsbn("12345");
    }

    @Test
    public void testGetOnlyBooks_Success() {
        // Arrange
        User currentUser = new User();
        currentUser.setUsername("testUser");
        currentUser.setRole(Role.READER);

        mockCurrentUser(currentUser);
        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));

        Book book1 = new Book("Book Title 1", "Author 1", "12345", "15.00", new Date(), "url1");
        Book book2 = new Book("Book Title 2", "Author 2", "67890", "20.00", new Date(), "url2");

        List<Book> books = Arrays.asList(book1, book2);

        Pageable paging = PageRequest.of(0, 10);
        Page<Book> booksPage = new PageImpl<>(books, paging, books.size());

        when(bookRepository.findAll(paging)).thenReturn(booksPage);

        // Act
        ResponseEntity<Set<BookDetailWithoutUserListDto>> response = bookService.getOnlyBooks(0, 10);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        verify(bookRepository, times(1)).findAll(paging);
    }

    @Test
    public void testGetOnlyBooks_Unauthorized() {
        // Arrange
        mockCurrentUser(null);

        // Act
        ResponseEntity<Set<BookDetailWithoutUserListDto>> response = bookService.getOnlyBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(bookRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    public void testGetOnlyBooks_Forbidden() {
        // Arrange
        User currentUser = new User();
        currentUser.setRole(Role.ADMIN);
        mockCurrentUser(currentUser);
        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));

        // Act
        ResponseEntity<Set<BookDetailWithoutUserListDto>> response = bookService.getOnlyBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(bookRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    public void testGetBooksByUserId_Success() {
        // Arrange
        User user = new User();
        user.setId(1);
        user.setBooks(Set.of(
                new Book("Book 1", "Author 1", "12345", "10.00", new Date(), "url1"),
                new Book("Book 2", "Author 2", "67890", "15.00", new Date(), "url2")
        ));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        // Act
        Page<BookDetailWithoutUserListDto> result = bookService.getBooksByUserId(1, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    public void testGetBooksByUserId_UserNotFound() {
        // Arrange
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act
        Page<BookDetailWithoutUserListDto> result = bookService.getBooksByUserId(99, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(userRepository, times(1)).findById(99);
    }

    @Test
    public void testGetBooksByUserIdentifier_SuccessByUsername() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setBooks(Set.of(
                new Book("Book 1", "Author 1", "12345", "10.00", new Date(), "url1"),
                new Book("Book 2", "Author 2", "67890", "15.00", new Date(), "url2")
        ));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Act
        Page<BookDetailWithoutUserListDto> result = bookService.getBooksByUserIdentifier("testuser", 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    public void testGetBooksByUserIdentifier_SuccessByEmail() {
        // Arrange
        User user = new User();
        user.setEmail("testuser@test.com");
        user.setBooks(Set.of(
                new Book("Book 1", "Author 1", "12345", "10.00", new Date(), "url1"),
                new Book("Book 2", "Author 2", "67890", "15.00", new Date(), "url2")
        ));
        when(userRepository.findByUsername("invalid")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("testuser@test.com")).thenReturn(Optional.of(user));

        // Act
        Page<BookDetailWithoutUserListDto> result = bookService.getBooksByUserIdentifier("testuser@test.com", 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(userRepository, times(1)).findByEmail("testuser@test.com");
    }

    @Test
    public void testGetBooksByUserIdentifier_UserNotFound() {
        // Arrange
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act
        Page<BookDetailWithoutUserListDto> result = bookService.getBooksByUserIdentifier("unknownuser", 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(userRepository, times(1)).findByUsername("unknownuser");
        verify(userRepository, times(1)).findByEmail("unknownuser");
    }

    @Test
    public void testUpdateBook_Unauthorized() throws IOException {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Book> response = bookService.updateBook("12345", "isbn", new Book());

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    public void testUpdateBook_Forbidden() throws IOException {
        // Arrange
        User currentUser = new User();
        currentUser.setRole(Role.GUEST);  // Assuming GUEST has no permission to update books
        mockCurrentUser(currentUser);
        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));

        // Act
        ResponseEntity<Book> response = bookService.updateBook("12345", "isbn", new Book());

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    public void testUpdateBook_BookNotFoundException() throws IOException {
        // Arrange
        User currentUser = new User();
        currentUser.setRole(Role.ADMIN);
        mockCurrentUser(currentUser);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(bookRepository.findBookByIsbn("12345")).thenThrow(new BookNotFoundException("Book not found with ISBN: 12345", null));

        Book newBookDetails = new Book("New Title", "New Author", "12345", "15.00", new Date(), "newUrl");

        // Act
        ResponseEntity<Book> response = bookService.updateBook("12345", "isbn", newBookDetails);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    public void testUpdateBook_Success() throws IOException {
        // Arrange
        User currentUser = new User();
        currentUser.setRole(Role.ADMIN);
        mockCurrentUser(currentUser);
        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));

        Book existingBook = new Book("Title", "Author", "12345", "10.00", new Date(), "url1");
        existingBook.setId(1);
        when(bookRepository.findBookByIsbn("12345")).thenReturn(Optional.of(existingBook));

        Book newBookDetails = new Book("New Title", "New Author", "12345", "15.00", new Date(), "url2");
        newBookDetails.setId(existingBook.getId());

        // Act
        ResponseEntity<Book> response = bookService.updateBook("12345", "isbn", newBookDetails);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("New Title", Objects.requireNonNull(response.getBody()).getTitle());
        assertEquals("12345", response.getBody().getIsbn());
        verify(bookRepository, times(1)).save(newBookDetails);
    }


    @Test
    public void testDeleteBook_Unauthorized() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Void> response = bookService.deleteBook("12345", "isbn");

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    public void testDeleteBook_Forbidden() {
        // Arrange
        User currentUser = new User();
        currentUser.setRole(Role.GUEST);  // Assuming GUEST has no permission to delete books
        mockCurrentUser(currentUser);
        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));

        // Act
        ResponseEntity<Void> response = bookService.deleteBook("12345", "isbn");

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    public void testDeleteBook_BookNotFoundException() {
        // Arrange
        User currentUser = new User();
        currentUser.setRole(Role.ADMIN);
        mockCurrentUser(currentUser);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(bookRepository.findBookByIsbn("12345")).thenThrow(new BookNotFoundException("Book not found with ISBN: 12345", null));

        // Act
        ResponseEntity<Void> response = bookService.deleteBook("12345", "isbn");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    public void testDeleteBook_Success() {
        // Arrange
        User currentUser = new User();
        currentUser.setRole(Role.ADMIN);
        mockCurrentUser(currentUser);
        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));

        Book bookToDelete = new Book("Title", "Author", "12345", "10.00", new Date(), "url1");
        when(bookRepository.findBookByIsbn("12345")).thenReturn(Optional.of(bookToDelete));

        // Act
        ResponseEntity<Void> response = bookService.deleteBook("12345", "isbn");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(bookRepository, times(1)).delete(bookToDelete);
    }

    @Test
    public void testSaveAll_Success() {
        // Arrange
        List<Book> books = Arrays.asList(
                new Book("Book1", "Author1", "11111", "10.00", new Date(), "url1"),
                new Book("Book2", "Author2", "22222", "15.00", new Date(), "url2")
        );

        // Act
        bookService.saveAll(books);

        // Assert
        verify(bookRepository, times(1)).saveAll(books);
    }

    @Test
    public void testAddBookToCurrentUser_Success() {
        // Arrange
        User currentUser = new User();
        currentUser.setUsername("testuser");
        currentUser.setRole(Role.ADMIN);

        Book book = new Book();
        currentUser.setBooks(new HashSet<>());
        mockCurrentUser(currentUser);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(bookRepository.findBookByIsbn("12345")).thenReturn(Optional.of(book));
        when(userRepository.save(currentUser)).thenReturn(currentUser);

        // Act
        ResponseEntity<Object> response = bookService.addBookToCurrentUser("12345", "isbn");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository, times(1)).save(currentUser);
    }

    @Test
    public void testAddBookToCurrentUser_ConcurrentModificationException() {
        // Arrange
        User currentUser = new User();
        currentUser.setUsername("testuser");
        currentUser.setRole(Role.ADMIN);
        Book book = new Book();
        currentUser.setBooks(new HashSet<>());

        mockCurrentUser(currentUser);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(bookRepository.findBookByIsbn("12345")).thenReturn(Optional.of(book));

        doThrow(new ConcurrentModificationException()).when(userRepository).save(currentUser);

        // Act
        ResponseEntity<Object> response = bookService.addBookToCurrentUser("12345", "isbn");

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Concurrent modification detected. Please try again.", response.getBody());
        verify(userRepository, times(MAX_RETRY_ATTEMPTS)).save(currentUser);
    }

    @Test
    public void testAddBookToCurrentUser_InternalServerError() {
        // Arrange
        User currentUser = new User();
        currentUser.setUsername("testuser");
        currentUser.setRole(Role.ADMIN);

        mockCurrentUser(currentUser);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));

        doThrow(new NullPointerException("Unexpected error")).when(userRepository).save(currentUser);

        // Act
        ResponseEntity<Object> response = bookService.addBookToCurrentUser("12345", "isbn");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(((String)response.getBody()).contains("An unexpected error occurred"));
    }

    @Test
    public void testAddBookToCurrentUser_BookAlreadyInCollection() {
        // Arrange
        User currentUser = new User();
        currentUser.setUsername("testuser");
        currentUser.setRole(Role.ADMIN);

        Book book = new Book();
        currentUser.setBooks(new HashSet<>(Collections.singleton(book)));
        mockCurrentUser(currentUser);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(bookRepository.findBookByIsbn("12345")).thenReturn(Optional.of(book));

        // Act
        ResponseEntity<Object> response = bookService.addBookToCurrentUser("12345", "isbn");

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Book already in user's collection", response.getBody());
        verify(userRepository, never()).save(currentUser);
    }

    @Test
    public void testAddBookToCurrentUser_UserNotFound() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Object> response = bookService.addBookToCurrentUser("12345", "isbn");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Current user not found", response.getBody());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testAddBookToCurrentUser_BookNotFoundException() {
        // Arrange
        User currentUser = new User();
        currentUser.setUsername("testuser");
        currentUser.setRole(Role.ADMIN);

        mockCurrentUser(currentUser);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(bookRepository.findBookByIsbn("12345")).thenThrow(new BookNotFoundException("Book not found with ISBN: 12345", null));

        // Act
        ResponseEntity<Object> response = bookService.addBookToCurrentUser("12345", "isbn");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Book not found with ISBN: 12345", response.getBody());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testDeleteBookFromCurrentUser_Success() {
        // Arrange
        User currentUser = new User();
        currentUser.setUsername("testuser");
        currentUser.setRole(Role.ADMIN);

        Book book = new Book();
        currentUser.setBooks(new HashSet<>(Collections.singleton(book)));
        mockCurrentUser(currentUser);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(bookRepository.findBookByIsbn("12345")).thenReturn(Optional.of(book));
        when(userRepository.save(currentUser)).thenReturn(currentUser);

        // Act
        ResponseEntity<Object> response = bookService.deleteBookFromCurrentUser("12345", "isbn");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userRepository, times(1)).save(currentUser);
    }


    @Test
    public void testDeleteBookFromCurrentUser_OptimisticLockException() {
        // Arrange
        User currentUser = new User();
        currentUser.setUsername("testuser");
        currentUser.setRole(Role.ADMIN);

        Book bookToDelete = new Book();
        bookToDelete.setIsbn("12345");
        currentUser.setBooks(new HashSet<>(Collections.singleton(bookToDelete)));

        mockCurrentUser(currentUser);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(bookRepository.findBookByIsbn("12345")).thenReturn(Optional.of(bookToDelete));

        // Simulate OptimisticLockException on save
        doThrow(new OptimisticLockException()).when(userRepository).save(currentUser);

        // Act
        ResponseEntity<Object> response = bookService.deleteBookFromCurrentUser("12345", "isbn");

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Conflict occurred while deleting the book from user. Please try again.", response.getBody());
        verify(userRepository, times(MAX_RETRY_ATTEMPTS)).save(currentUser);
    }

    @Test
    public void testDeleteBookFromCurrentUser_BookNotInCollection() {
        // Arrange
        User currentUser = new User();
        currentUser.setUsername("testuser");
        currentUser.setRole(Role.ADMIN);
        currentUser.setBooks(new HashSet<>());
        mockCurrentUser(currentUser);

        Book book = new Book();

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(bookRepository.findBookByIsbn("12345")).thenReturn(Optional.of(book));

        // Act
        ResponseEntity<Object> response = bookService.deleteBookFromCurrentUser("12345", "isbn");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Book not found in user's collection", response.getBody());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testDeleteBookFromCurrentUser_UserNotFound() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Object> response = bookService.deleteBookFromCurrentUser("12345", "isbn");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Current user not found", response.getBody());
        verify(userRepository, never()).save(any(User.class));
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
