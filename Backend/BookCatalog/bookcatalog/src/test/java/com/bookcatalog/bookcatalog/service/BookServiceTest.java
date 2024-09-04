package com.bookcatalog.bookcatalog.service;

import com.bookcatalog.bookcatalog.exceptions.BookNotFoundException;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
import com.bookcatalog.bookcatalog.model.dto.UserDto;
import com.bookcatalog.bookcatalog.repository.UserRepository;
import com.bookcatalog.bookcatalog.service.strategy.StrategyFactory;
import com.bookcatalog.bookcatalog.service.strategy.delete.DeleteStrategy;
import com.bookcatalog.bookcatalog.service.strategy.update.UpdateStrategy;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
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

import com.bookcatalog.bookcatalog.model.Book;
import com.bookcatalog.bookcatalog.repository.BookRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
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
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserDetails userDetails;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UpdateStrategy<Book> updateStrategy;

    @Mock
    private DeleteStrategy<Book> deleteStrategy;

    @Mock
    private StrategyFactory<Book> strategyFactory;

    @Mock
    private UserService mockUserService;

    @Mock
    private UserDto mockUserDto;

    @InjectMocks
    private BookService bookService;

    @InjectMocks
    private UserService userService;

    private User currentUser;
    private Book book;
    private Book newDetails;

    private User mockUser;
    private Book mockBook;



    @BeforeEach
    public void setUp() {

        MockitoAnnotations.openMocks(this);

        //userService = new UserService(userRepository, passwordEncoder, strategyFactoryUser);
        bookService = new BookService(userRepository, bookRepository, strategyFactory, userService);


        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);

        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        // Setup mock users
        currentUser = new User();
        currentUser.setUsername("currentUser");
        currentUser.setRole(Role.ADMIN);

        book = new Book(1, "Title", "Author");
        newDetails = new Book(1, "New Title", "New Author");

        // Initialize mock user and book
        mockUser = new User();
        mockUser.setUsername("testUser");


        mockBook = new Book();
        mockBook.setId(7);

        mockUser.setBooks(new HashSet<>());
        mockUser.getBooks().add(mockBook);
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
    void testCreateBook_Success() {

        // Arrange
        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor("Test Author");

        Book savedBook = new Book();
        savedBook.setId(1);
        savedBook.setTitle("Test Book");
        savedBook.setAuthor("Test Author");

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        // Act
        Book result = bookService.createBook(book);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Book", result.getTitle());
        assertEquals("Test Author", result.getAuthor());

        verify(bookRepository, times(1)).save(book);
    }

    @Test
    void testCreateBookWithNullBookRepository_Failure() {

        // Arrange
        bookService = new BookService(userRepository, null, strategyFactory, userService);

        // Act & Assert
        Book book = new Book();
        book.setTitle("Test Book");
        book.setAuthor("Test Author");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            bookService.createBook(book);
        });

        assertEquals("BookRepository is not initialized", exception.getMessage());
    }

    @Test
    public void testGetBook_ByTitle_Success() {
        // Arrange
        User user = new User();
        user.setRole(Role.READER);
        mockCurrentUser(user);

        Book book = new Book("Title1", "Author1");
        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(bookRepository.findBookByTitle("Title1")).thenReturn(Optional.of(book));

        // Act
        Book result = bookService.getBook("Title1", "title");

        // Assert
        assertEquals(book, result);
    }

    @Test
    public void testGetBook_ByTitle_NotFound() {
        // Arrange
        User user = new User();
        user.setRole(Role.READER);
        mockCurrentUser(user);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(bookRepository.findBookByTitle("UnknownTitle")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BookNotFoundException.class, () -> bookService.getBook("UnknownTitle", "title"));
    }

    @Test
    public void testGetBook_InvalidType() {
        // Arrange
        User user = new User();
        user.setRole(Role.READER);
        mockCurrentUser(user);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> bookService.getBook("1", "invalidType"));
    }

    @Test
    public void testGetBook_ShouldThrowIllegalStateException() {
        // Arrange
        User user = new User();
        user.setRole(Role.READER);
        mockCurrentUser(user);

        when(userService.getCurrentUser()).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(IllegalStateException.class, () -> bookService.getBook("1", "id"));
    }

    @Test
    public void testGetBooksByAuthor_BooksFound() {
        // Arrange
        String author = "Author";
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);

        List<Book> books = List.of(
                new Book("Title1", author, "1234567890", "10.99", null, null, null),
                new Book("Title2", author, "0987654321", "12.99", null, null, null)
        );

        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        when(bookRepository.findBooksByAuthor(author, pageable)).thenReturn(bookPage);

        // Act
        Page<Book> result = bookService.getBooksByAuthor(author, page, size);

        // Assert
        assertEquals(books.size(), result.getTotalElements());
        assertEquals(books, result.getContent());
        verify(bookRepository, times(1)).findBooksByAuthor(author, pageable);
    }

    @Test
    public void testGetBooksByAuthor_NoBooksFound() {
        // Arrange
        String author = "Author";
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(bookRepository.findBooksByAuthor(author, pageable)).thenReturn(emptyPage);

        // Act
        Page<Book> result = bookService.getBooksByAuthor(author, page, size);

        // Assert
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
        verify(bookRepository, times(1)).findBooksByAuthor(author, pageable);
    }

    @Test
    public void testGetAllBooks_UserSuper_Success() throws IOException {

        // Arrange
        User user = new User();
        user.setRole(Role.SUPER);
        mockCurrentUser(user);

        Book book1 = new Book("Title1", "Author1", "000000000000", "10.00", new Date(), "cover1.jpg");
        Book book2 = new Book("Title2", "Author2", "000000000001", "15.00", new Date(), "cover2.jpg");
        List<Book> books = Arrays.asList(book1, book2);
        Page<Book> booksPage = new PageImpl<>(books);

        when(bookRepository.findAll(any(Pageable.class))).thenReturn(booksPage);

        // Act
        ResponseEntity<Page<Book>> response = bookService.getAllBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).getTotalElements());
    }

    @Test
    public void testGetAllBooks_UserAdmin_Success() throws IOException {

        // Arrange
        User user = new User();
        user.setRole(Role.ADMIN);
        mockCurrentUser(user);

        Book book1 = new Book("Title1", "Author1", "000000000000", "10.00", new Date(), "cover1.jpg");
        Book book2 = new Book("Title2", "Author2", "000000000001", "15.00", new Date(), "cover2.jpg");
        List<Book> books = Arrays.asList(book1, book2);
        Page<Book> booksPage = new PageImpl<>(books);

        when(bookRepository.findAll(any(Pageable.class))).thenReturn(booksPage);

        // Act
        ResponseEntity<Page<Book>> response = bookService.getAllBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, Objects.requireNonNull(response.getBody()).getTotalElements());
    }

    @Test
    public void testGetAllBooks_UserReader_Failure() throws IOException {

        // Arrange
        User user = new User();
        user.setUsername("user");
        user.setRole(Role.READER);
        mockCurrentUser(user);

        // Act
        ResponseEntity<Page<Book>> response = bookService.getAllBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testGetAllBooks_UserGuest_Failure() throws IOException {

        // Arrange
        User user = new User();
        user.setUsername("user");
        user.setRole(Role.GUEST);
        mockCurrentUser(user);

        // Act
        ResponseEntity<Page<Book>> response = bookService.getAllBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testGetAllBooks_UserIsNull_Failure() throws IOException {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(Optional.empty());
        // Act
        ResponseEntity<Page<Book>> response = bookService.getAllBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetAllBooks_UserRoleIsNull_Failure() throws IOException {

        // Arrange
        User user = new User();
        user.setUsername("user");
        user.setRole(null);
        mockCurrentUser(user);

        // Act
        ResponseEntity<Page<Book>> response = bookService.getAllBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testGetAllBooks_ThrowsException() {

        // Arrange
        User user = new User();
        user.setUsername("user");
        user.setRole(Role.ADMIN);
        mockCurrentUser(user);

        // Act
        when(bookRepository.findAll(any(Pageable.class))).thenThrow(new RuntimeException("An error occurred while fetching books"));
        // Assert
    }

    @Test
    void testGetAllBooks_IllegalStateException() throws IOException {
        // Arrange
        int page = 0;
        int size = 10;

        // Mocking userService to throw IllegalStateException
        when(userService.getCurrentUser()).thenThrow(new IllegalStateException("Illegal state encountered"));

        // Act
        ResponseEntity<Page<Book>> response = bookService.getAllBooks(page, size);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verifyNoInteractions(bookRepository);  // Ensure bookRepository is not called
    }

    @Test
    public void testGetBooksByUserId_UserFound() throws ParseException {
        // Arrange
        Integer userId = 1;
        User user = new User();
        Set<User> users = new HashSet<>();
        users.add(user);

        Set<Book> books = new HashSet<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date date1 = dateFormat.parse("23-08-2001");
        Date date2 = dateFormat.parse("07-12-1978");
        books.add(new Book("Title1", "Author1", "ISBN1", "10.0", date1, "coverImage1", users));
        books.add(new Book("Title2", "Author2", "ISBN2", "20.0", date2, "coverImage2", users));
        user.setBooks(books);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        Page<Book> result = bookService.getBooksByUserId(userId,0 , 10);

        // Assert
        assertEquals(2, result.getTotalElements());

        Book book1 = books.stream().filter(book -> book.getTitle().equals("Title1")).findFirst().orElse(null);
        Book book2 = books.stream().filter(book -> book.getTitle().equals("Title2")).findFirst().orElse(null);

        Book dto1 = result.stream().filter(dto -> dto.getTitle().equals("Title1")).findFirst().orElse(null);
        Book dto2 = result.stream().filter(dto -> dto.getTitle().equals("Title2")).findFirst().orElse(null);

        assertNotNull(book1);
        assertNotNull(book2);
        assertNotNull(dto1);
        assertNotNull(dto2);

        assertEquals(book1.getTitle(), dto1.getTitle());
        assertEquals(book1.getAuthor(), dto1.getAuthor());
        assertEquals(book1.getIsbn(), dto1.getIsbn());
        assertEquals(book1.getPublishDate(), dto1.getPublishDate());
        assertEquals(book1.getPrice(), dto1.getPrice());

        assertEquals(book2.getTitle(), dto2.getTitle());
        assertEquals(book2.getAuthor(), dto2.getAuthor());
        assertEquals(book2.getIsbn(), dto2.getIsbn());
        assertEquals(book2.getPublishDate(), dto2.getPublishDate());
        assertEquals(book2.getPrice(), dto2.getPrice());
    }

    @Test
    public void testGetBooksByUserId_UserFound_StartPageIsGreater_ReturnsEmptyList() throws ParseException {
        // Arrange
        Integer userId = 1;
        User user = new User();
        Set<User> users = new HashSet<>();
        users.add(user);

        Set<Book> books = new HashSet<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date date1 = dateFormat.parse("23-08-2001");
        Date date2 = dateFormat.parse("07-12-1978");
        books.add(new Book("Title1", "Author1", "ISBN1", "10.0", date1, "coverImage1", users));
        books.add(new Book("Title2", "Author2", "ISBN2", "20.0", date2, "coverImage2", users));
        user.setBooks(books);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        Page<Book> result = bookService.getBooksByUserId(userId,2 , 10);

        // Assert
        assertTrue(result.getContent().isEmpty());
        assertEquals(books.size(), result.getTotalElements());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    public void testGetBooksByUserId_UserNotFound() {
        // Arrange
        Integer userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        Page<Book> result = bookService.getBooksByUserId(userId, 0, 10);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetBooksByUserIdentifier_UserFoundByUsername() throws ParseException {
        // Arrange
        String identifier = "username";
        User user = new User();
        Set<User> users = new HashSet<>();
        users.add(user);

        Set<Book> books = new HashSet<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date date1 = dateFormat.parse("23-08-2001");
        Date date2 = dateFormat.parse("07-12-1978");
        books.add(new Book("Title1", "Author1", "ISBN1", "10.0", date1, "coverImage1", users));
        books.add(new Book("Title2", "Author2", "ISBN2", "20.0", date2, "coverImage2", users));
        user.setBooks(books);

        when(userRepository.findByUsername(identifier)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.empty());

        // Act
        Page<Book> result = bookService.getBooksByUserIdentifier(identifier, 0, 10);

        // Assert
        assertEquals(2, result.getTotalElements());
        Book book1 = books.stream().filter(book -> book.getTitle().equals("Title1")).findFirst().orElse(null);
        Book book2 = books.stream().filter(book -> book.getTitle().equals("Title2")).findFirst().orElse(null);

        Book dto1 = result.stream().filter(dto -> dto.getTitle().equals("Title1")).findFirst().orElse(null);
        Book dto2 = result.stream().filter(dto -> dto.getTitle().equals("Title2")).findFirst().orElse(null);

        assertNotNull(book1);
        assertNotNull(book2);
        assertNotNull(dto1);
        assertNotNull(dto2);

        assertEquals(book1.getTitle(), dto1.getTitle());
        assertEquals(book1.getAuthor(), dto1.getAuthor());
        assertEquals(book1.getIsbn(), dto1.getIsbn());
        assertEquals(book1.getPublishDate(), dto1.getPublishDate());
        assertEquals(book1.getPrice(), dto1.getPrice());

        assertEquals(book2.getTitle(), dto2.getTitle());
        assertEquals(book2.getAuthor(), dto2.getAuthor());
        assertEquals(book2.getIsbn(), dto2.getIsbn());
        assertEquals(book2.getPublishDate(), dto2.getPublishDate());
        assertEquals(book2.getPrice(), dto2.getPrice());
    }

    @Test
    public void testGetBooksByUserIdentifier_UserFoundByUsername_StartPageIsGreater_ReturnsEmptyList() throws ParseException {
        // Arrange
        String identifier = "username";
        User user = new User();
        Set<User> users = new HashSet<>();
        users.add(user);

        Set<Book> books = new HashSet<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date date1 = dateFormat.parse("23-08-2001");
        Date date2 = dateFormat.parse("07-12-1978");
        books.add(new Book("Title1", "Author1", "ISBN1", "10.0", date1, "coverImage1", users));
        books.add(new Book("Title2", "Author2", "ISBN2", "20.0", date2, "coverImage2", users));
        user.setBooks(books);

        when(userRepository.findByUsername(identifier)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.empty());

        // Act
        Page<Book> result = bookService.getBooksByUserIdentifier(identifier, 2, 10);

        // Assert
        assertTrue(result.getContent().isEmpty());
        assertEquals(books.size(), result.getTotalElements());
        verify(userRepository, times(1)).findByUsername(identifier);
    }

    @Test
    public void testGetBooksByUserIdentifier_UserFoundByEmail() throws ParseException {
        // Arrange
        String identifier = "email@example.com";
        User user = new User();
        Set<User> users = new HashSet<>();
        users.add(user);

        Set<Book> books = new HashSet<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date date1 = dateFormat.parse("23-08-2001");
        Date date2 = dateFormat.parse("07-12-1978");
        books.add(new Book("Title1", "Author1", "ISBN1", "10.0", date1, "coverImage1", users));
        books.add(new Book("Title2", "Author2", "ISBN2", "20.0", date2, "coverImage2", users));
        user.setBooks(books);

        when(userRepository.findByUsername(identifier)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.of(user));

        // Act
        Page<Book> result = bookService.getBooksByUserIdentifier(identifier, 0, 10);

        // Assert
        assertEquals(2, result.getTotalElements());
        Book book1 = books.stream().filter(book -> book.getTitle().equals("Title1")).findFirst().orElse(null);
        Book book2 = books.stream().filter(book -> book.getTitle().equals("Title2")).findFirst().orElse(null);
        Book dto1 = result.stream().filter(dto -> dto.getTitle().equals("Title1")).findFirst().orElse(null);
        Book dto2 = result.stream().filter(dto -> dto.getTitle().equals("Title2")).findFirst().orElse(null);

        assertNotNull(book1);
        assertNotNull(book2);
        assertNotNull(dto1);
        assertNotNull(dto2);

        assertEquals(book1.getTitle(), dto1.getTitle());
        assertEquals(book1.getAuthor(), dto1.getAuthor());
        assertEquals(book1.getIsbn(), dto1.getIsbn());
        assertEquals(book1.getPublishDate(), dto1.getPublishDate());
        assertEquals(book1.getPrice(), dto1.getPrice());

        assertEquals(book2.getTitle(), dto2.getTitle());
        assertEquals(book2.getAuthor(), dto2.getAuthor());
        assertEquals(book2.getIsbn(), dto2.getIsbn());
        assertEquals(book2.getPublishDate(), dto2.getPublishDate());
        assertEquals(book2.getPrice(), dto2.getPrice());
    }

    @Test
    public void testGetBooksByUserIdentifier_UserFoundByEmail_StartPageIsGreater_ReturnsEmptyList() throws ParseException {
        // Arrange
        String identifier = "email@example.com";
        User user = new User();
        Set<User> users = new HashSet<>();
        users.add(user);

        Set<Book> books = new HashSet<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date date1 = dateFormat.parse("23-08-2001");
        Date date2 = dateFormat.parse("07-12-1978");
        books.add(new Book("Title1", "Author1", "ISBN1", "10.0", date1, "coverImage1", users));
        books.add(new Book("Title2", "Author2", "ISBN2", "20.0", date2, "coverImage2", users));
        user.setBooks(books);

        when(userRepository.findByUsername(identifier)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.of(user));

        // Act
        Page<Book> result = bookService.getBooksByUserIdentifier(identifier, 2, 10);

        // Assert
        assertTrue(result.getContent().isEmpty());
        assertEquals(books.size(), result.getTotalElements());
        verify(userRepository, times(1)).findByEmail(identifier);
    }

    @Test
    public void testGetBooksByUserIdentifier_UserNotFound() {
        // Arrange
        String identifier = "nonexistent";
        when(userRepository.findByUsername(identifier)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(identifier)).thenReturn(Optional.empty());

        // Act
        Page<Book> result = bookService.getBooksByUserIdentifier(identifier, 0, 10);

        // Assert
        assertTrue(result.isEmpty());
    }


    @Test
    public void testUpdateBook_Success() throws IOException {
        // Arrange
        User user = new User();
        user.setRole(Role.ADMIN);
        mockCurrentUser(user);

        Book existingBook = new Book("Title1", "Author1");
        Book newBookDetails = new Book("NewTitle", "NewAuthor");

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(strategyFactory.getUpdateStrategy("title")).thenReturn(updateStrategy);
        when(bookRepository.findBookByTitle("Title1")).thenReturn(Optional.of(existingBook));

        // Act
        ResponseEntity<Void> response = bookService.updateBook("Title1", "title", newBookDetails, "filename.txt");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(updateStrategy, times(1)).update(existingBook, newBookDetails, "filename.txt");
    }

    @Test
    public void testUpdateBook_UserNotFound_ShouldReturnUnauthorized() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Void> response = bookService.updateBook("1", "id", new Book(), "filename");

        // Assert
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testUpdateBook_UserWithoutPermission_ShouldReturnForbidden() {
        User user = new User();
        user.setRole(Role.READER);
        mockCurrentUser(user);

        when(strategyFactory.getUpdateStrategy(anyString())).thenReturn(updateStrategy);
        when(userService.getCurrentUser()).thenReturn(Optional.of(user));

        ResponseEntity<Void> response = bookService.updateBook("1", "id", new Book(), "filename");

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testUpdateBook_InvalidStrategy_ShouldReturnBadRequest() {
        // Arrange
        User user = new User();
        user.setRole(Role.ADMIN);
        mockCurrentUser(user);

        when(userService.getCurrentUser()).thenReturn(Optional.of(user));
        when(strategyFactory.getUpdateStrategy(anyString())).thenReturn(null);

        // Act
        ResponseEntity<Void> response = bookService.updateBook("1", "id", new Book(), "filename");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void updateBook_BookNotFound() {
        // Arrange
        User user = new User();
        user.setRole(Role.ADMIN);
        mockCurrentUser(user);

        when(userService.getCurrentUser()).thenReturn(Optional.of(user));
        when(strategyFactory.getUpdateStrategy(anyString())).thenReturn(updateStrategy);
        when(bookRepository.findById(anyInt())).thenThrow(new BookNotFoundException("Book not found", null));

        // Act
        ResponseEntity<Void> response = bookService.updateBook("8", "id", new Book(), "filename");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateBook_UpdateSuccessful() throws IOException {
        // Arrange
        String identifier = "123";
        String type = "id";

        Book newBookDetails = new Book();
        String filename = "new-cover.jpg";

        Book existingBook = new Book();
        existingBook.setId(123);
        existingBook.setTitle("Old Title");

        User currentUser = new User();
        currentUser.setRole(Role.ADMIN);
        mockCurrentUser(currentUser);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(bookRepository.getReferenceById(any())).thenReturn(existingBook);
        when(strategyFactory.getUpdateStrategy(type)).thenReturn(updateStrategy);

        // Act
        ResponseEntity<Void> response = bookService.updateBook(identifier, type, newBookDetails, filename);

        // Assert
        verify(updateStrategy).update(existingBook, newBookDetails, filename);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void updateBook_InternalServerError() throws IOException {
        // Arrange
        String identifier = "123";
        String type = "id";
        Book newBookDetails = new Book();
        String filename = "new-cover.jpg";

        Book existingBook = new Book();
        existingBook.setId(123);

        User currentUser = new User();
        currentUser.setRole(Role.ADMIN);
        mockCurrentUser(currentUser);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(bookRepository.getReferenceById(any())).thenReturn(existingBook);
        when(strategyFactory.getUpdateStrategy(type)).thenReturn(updateStrategy);

        doThrow(new IllegalArgumentException("Invalid argument")).when(updateStrategy).update(any(), any(), any());

        // Act
        ResponseEntity<Void> response = bookService.updateBook(identifier, type, newBookDetails, filename);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testDeleteBook_Success() throws IOException {
        // Arrange
        User user = new User();
        user.setRole(Role.ADMIN);
        mockCurrentUser(user);

        Book book = new Book("Title1", "Author1");
        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(strategyFactory.getDeleteStrategy("title")).thenReturn(deleteStrategy);
        when(bookRepository.findBookByTitle("Title1")).thenReturn(Optional.of(book));

        // Act
        ResponseEntity<Void> response = bookService.deleteBook("Title1", "title");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deleteStrategy, times(1)).delete(book);
    }

    @Test
    public void testDeleteBook_UserNotFound_ShouldReturnUnauthorized() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Void> response = bookService.deleteBook("1", "id");

        // Arrange
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testDeleteBook_UserWithoutPermission_ShouldReturnForbidden() {
        // Arrange
        User user = new User();
        user.setRole(Role.READER);
        mockCurrentUser(user);

        when(strategyFactory.getDeleteStrategy("id")).thenReturn(deleteStrategy);
        when(userService.getCurrentUser()).thenReturn(Optional.of(user));

        // Act
        ResponseEntity<Void> response = bookService.deleteBook("1", "id");

        // Arrange
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void testDeleteBook_InvalidStrategy_ShouldReturnBadRequest() {
        // Arrange
        User user = new User();
        user.setRole(Role.ADMIN);
        mockCurrentUser(user);

        when(userService.getCurrentUser()).thenReturn(Optional.of(user));
        when(strategyFactory.getDeleteStrategy(anyString())).thenReturn(null);

        // Act
        ResponseEntity<Void> response = bookService.deleteBook("1", "id");

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testDeleteBook_BookNotFound_ShouldReturnNotFound() throws IOException {
        // Arrange
        User user = new User();
        user.setRole(Role.ADMIN);
        mockCurrentUser(user);

        when(userService.getCurrentUser()).thenReturn(Optional.of(user));
        when(strategyFactory.getDeleteStrategy(anyString())).thenReturn(deleteStrategy);
        when(bookRepository.getReferenceById(anyInt())).thenThrow(new EntityNotFoundException());

        // Act
        ResponseEntity<Void> response = bookService.deleteBook("1", "id");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(deleteStrategy, never()).delete(any());
    }

    @Test
    public void testDeleteBook_SuccessfulDelete_ShouldReturnOk() throws IOException {
        // Arrange
        User user = new User();
        user.setRole(Role.ADMIN);
        mockCurrentUser(user);

        // Arrange
        Book book = new Book("Title1", "Author1");
        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(strategyFactory.getDeleteStrategy("title")).thenReturn(deleteStrategy);
        when(bookRepository.findBookByTitle("Title1")).thenReturn(Optional.of(book));

        // Act
        ResponseEntity<Void> response = bookService.deleteBook("Title1", "title");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deleteStrategy, times(1)).delete(book);
    }

    @Test
    public void testDeleteBook_InternalServerError_ShouldReturnInternalServerError() throws IOException {
        // Arrange
        User user = new User();
        user.setRole(Role.ADMIN);
        mockCurrentUser(user);

        when(userService.getCurrentUser()).thenReturn(Optional.of(user));
        when(strategyFactory.getDeleteStrategy(anyString())).thenReturn(deleteStrategy);
        when(bookRepository.getReferenceById(anyInt())).thenReturn(new Book());
        doThrow(IllegalArgumentException.class).when(deleteStrategy).delete(any(Book.class));

        // Act
        ResponseEntity<Void> response = bookService.deleteBook("1", "id");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testSaveAll() {
        // Arrange
        List<Book> books = Arrays.asList(
                new Book("Title1", "Author1", "ISBN1", "10.0", null, null, null),
                new Book("Title2", "Author2", "ISBN2", "20.0", null, null, null)
        );

        // Act
        bookService.saveAll(books);

        // Verify
        verify(bookRepository).saveAll(books);
    }

    @Test
    void addBookToCurrentUser_Success() throws IOException {
        // Arrange
        User currentUser = new User();
        currentUser.setUsername("testname");
        currentUser.setRole(Role.ADMIN);
        mockCurrentUser(currentUser);

        Book book = new Book("Title One","Author One", "1234567890123", "10.40", new Date(), null);
        Set<Book> books = new HashSet<>();

        currentUser.setBooks(books);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(bookRepository.findBookByIsbn("1234567890123")).thenReturn(Optional.of(book));

        // Act
        ResponseEntity<Object> response = bookService.addBookToCurrentUser("1234567890123", "isbn");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void addBookToCurrentUser_NotFound_Failure() throws IOException {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Object> response = bookService.addBookToCurrentUser("12345", "ISBN");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Current user not found", response.getBody());
    }

    @Test
    void addBookToCurrentUser_BookNotFound_Failure() throws IOException {
        // Arrange
        User currentUser = new User();
        currentUser.setUsername("testname");
        currentUser.setRole(Role.ADMIN);
        mockCurrentUser(currentUser);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));

        String identifier = "1234567890123";
        String type = "isbn";

        when(bookRepository.findBookByIsbn(identifier)).thenThrow(new BookNotFoundException("Book not found with ISBN: " + identifier, null));

        // Act
        ResponseEntity<Object> response = bookService.addBookToCurrentUser(identifier, type);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Book not found with ISBN: " + identifier, response.getBody());
    }

    /*
    @Test
    void addBookToCurrentUser_UpdateUserThrowsIOException() throws IOException {
        // Arrange
        User currentUser = new User();
        currentUser.setUsername("testname");
        currentUser.setRole(Role.ADMIN);
        mockCurrentUser(currentUser);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));

        String identifier = "1234567890123";
        String type = "isbn";
        Book book = new Book("Title 1", "Author 1");

        when(bookRepository.findBookByIsbn(identifier)).thenReturn(Optional.of(book));
        when(userService.updateUser("testname", "username", any(UserDto.class))).thenThrow((new IOException("Database unavailable")));

        // Act
        ResponseEntity<Object> response = bookService.addBookToCurrentUser(identifier, type);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to update user after adding book: Database unavailable", response.getBody());
    }

     */

    @Test
    void testAddBookToCurrentUser_BookAlreadyInCollection() throws IOException {
        // Arrange
        String identifier = "1234567890123";
        String type = "isbn";
        User currentUser = new User();
        currentUser.setUsername("testUser");
        Book book = new Book("Title 1", "Author 1");
        currentUser.setBooks(new HashSet<>(Collections.singletonList(book)));
        mockCurrentUser(currentUser);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(bookRepository.findBookByIsbn(identifier)).thenReturn(Optional.of(book));

        // Act
        ResponseEntity<Object> response = bookService.addBookToCurrentUser(identifier, type);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Book already in user's collection", response.getBody());
        verify(bookRepository).findBookByIsbn(identifier);
    }

    @Test
    void testAddBookToCurrentUser_ThrowsGeneralException() throws IOException {
        // Arrange
        String identifier = "1234567890123";
        String type = "isbn";

        when(userService.getCurrentUser()).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<Object> response = bookService.addBookToCurrentUser(identifier, type);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred: Unexpected error", response.getBody());
    }

    @Test
    void testDeleteBookFromCurrentUser_Success() throws IOException {
        // Arrange
        Book bookToAddToSet = new Book("Title 1", "Author 1");
        Set<Book> books = new HashSet<>();

        User currentUser = new User();
        currentUser.setUsername("testname");
        currentUser.setRole(Role.ADMIN);
        currentUser.setBooks(books);
        currentUser.getBooks().add(bookToAddToSet);

        mockCurrentUser(currentUser);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(bookRepository.findBookByTitle("Title 1")).thenReturn(Optional.of(bookToAddToSet));

        Set<Book> expectedBooksAfterBookDeletion = new HashSet<>(currentUser.getBooks());
        expectedBooksAfterBookDeletion.remove(bookToAddToSet);

        mockUserDto.setUsername(currentUser.getUsername());
        mockUserDto.setBooks(expectedBooksAfterBookDeletion);

        // Act
        bookService.deleteBookFromCurrentUser("Title 1", "title");

        // Assert
        verify(bookRepository).findBookByTitle("Title 1");
        assertFalse(currentUser.getBooks().contains(bookToAddToSet), "Book should be removed from the user's collection");
    }

    @Test
    void testDeleteBookFromCurrentUser_UserNotFound() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Object> response = bookService.deleteBookFromCurrentUser("1234567890123", "isbn");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Current user not found", response.getBody());
    }

    @Test
    void testDeleteBookFromCurrentUser_BookNotFound() {
        // Arrange
        User currentUser = new User();
        currentUser.setUsername("testuser");
        currentUser.setRole(Role.READER);
        mockCurrentUser(currentUser);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(bookRepository.findBookByIsbn("1234567890123")).thenReturn(Optional.empty());

        // Act
        ResponseEntity<Object> response = bookService.deleteBookFromCurrentUser("1234567890123", "isbn");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Book not found with ISBN: 1234567890123", response.getBody());
    }

    @Test
    void testDeleteBookFromCurrentUser_BookNotInUserCollection() {
        // Arrange
        Book bookToRemove = new Book("Title 1", "Author 1");
        Set<Book> books = new HashSet<>();

        User currentUser = new User();
        currentUser.setUsername("testuser");
        currentUser.setRole(Role.READER);
        currentUser.setBooks(books);
        mockCurrentUser(currentUser);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(bookRepository.findBookByTitle("Title 1")).thenReturn(Optional.of(bookToRemove));

        // Act
        ResponseEntity<Object> response = bookService.deleteBookFromCurrentUser("Title 1", "title");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Book not found in user's collection", response.getBody());
    }

    @Test
    void testDeleteBookFromCurrentUser_UpdateUserFailure() throws IOException {
        // Arrange
        Book bookToRemove = new Book("Title 1", "Author 1");
        Set<Book> books = new HashSet<>();
        books.add(bookToRemove);

        User currentUser = new User();
        currentUser.setUsername("testuser");
        currentUser.setBooks(books);

        when(userService.getCurrentUser()).thenReturn(Optional.of(currentUser));
        when(bookRepository.findBookByTitle("Title 1")).thenReturn(Optional.of(bookToRemove));
        doThrow(new IOException("Update failed")).when(userService).updateUser(eq(currentUser.getUsername()), eq("username"), any(UserDto.class));

        // Act
        ResponseEntity<Object> response = bookService.deleteBookFromCurrentUser("Title 1", "title");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to update user after removing book: Update failed", response.getBody());
    }

    @Test
    void testDeleteBookFromCurrentUser_UnexpectedError() {
        // Arrange
        when(userService.getCurrentUser()).thenThrow(new RuntimeException("Unexpected error"));

        // Act
        ResponseEntity<Object> response = bookService.deleteBookFromCurrentUser("1234567890123", "isbn");

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred: Unexpected error", response.getBody());
    }


@AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
