package com.bookcatalog.bookcatalog.service;

import com.bookcatalog.bookcatalog.exceptions.BookNotFoundException;
import com.bookcatalog.bookcatalog.model.Role;
import com.bookcatalog.bookcatalog.model.User;
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

    @InjectMocks
    private BookService bookService;

    @InjectMocks
    private UserService userService;

    private User currentUser;
    private Book book;
    private Book newDetails;


    @BeforeEach
    public void setUp() {

        MockitoAnnotations.openMocks(this);

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
    void testGetBookById_EntityNotFoundException_ShouldThrowBookNotFoundException() {
        // Arrange
        String identifier = "1";
        String type = "id";

        when(bookRepository.getReferenceById(Integer.parseInt(identifier)))
                .thenThrow(new EntityNotFoundException("Book not found"));

        // Act and Assert
        assertThrows(BookNotFoundException.class, () -> bookService.getBook(identifier, type));
    }

    @Test
    void testGetBookByTitle_EntityNotFoundException_ShouldThrowEntityNotFoundException() {
        // Arrange
        String identifier = "Non-Existent Title";
        String type = "title";

        when(bookRepository.findBookByTitle(identifier))
                .thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(EntityNotFoundException.class, () -> bookService.getBook(identifier, type));
    }

    @Test
    void testGetBookByIsbn_EntityNotFoundException_ShouldThrowEntityNotFoundException() {
        // Arrange
        String identifier = "000-0-00-000000-0";
        String type = "isbn";

        when(bookRepository.findBookByIsbn(identifier))
                .thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(EntityNotFoundException.class, () -> bookService.getBook(identifier, type));
    }

    @Test
    void testGetBook_InvalidType_ShouldThrowIllegalArgumentException() {
        // Arrange
        String identifier = "1";
        String type = "invalidType";  // Invalid type

        // Act and Assert
        assertThrows(IllegalArgumentException.class, () -> bookService.getBook(identifier, type));
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
    public void testGetAllBooks_UserSuperOrAdmin_Success() throws IOException {

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
    public void testGetAllBooks_UserIsNull() throws IOException {

        // Arrange

        mockCurrentUser(null);
        when(userService.getCurrentUser()).thenReturn(Optional.empty());
        // Act
        ResponseEntity<Page<Book>> response = bookService.getAllBooks(0, 10);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(bookRepository, never()).findAll(any(Pageable.class));
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

        // Assert the size
        assertEquals(2, result.getTotalElements());

        // Assert the details of each book
        Book book1 = books.stream().filter(book -> book.getTitle().equals("Title1")).findFirst().orElse(null);
        Book book2 = books.stream().filter(book -> book.getTitle().equals("Title2")).findFirst().orElse(null);

        Book dto1 = result.stream().filter(dto -> dto.getTitle().equals("Title1")).findFirst().orElse(null);
        Book dto2 = result.stream().filter(dto -> dto.getTitle().equals("Title2")).findFirst().orElse(null);

        assertNotNull(book1);
        assertNotNull(book2);
        assertNotNull(dto1);
        assertNotNull(dto2);

        // Assert book1 and dto1 details
        assertEquals(book1.getTitle(), dto1.getTitle());
        assertEquals(book1.getAuthor(), dto1.getAuthor());
        assertEquals(book1.getIsbn(), dto1.getIsbn());
        assertEquals(book1.getPublishDate(), dto1.getPublishDate());
        assertEquals(book1.getPrice(), dto1.getPrice());

        // Assert book2 and dto2 details
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

        // Print the result to console
        System.out.println(result);

        // Assert the size
        assertEquals(2, result.getTotalElements());

        // Assert the details of each book
        Book book1 = books.stream().filter(book -> book.getTitle().equals("Title1")).findFirst().orElse(null);
        Book book2 = books.stream().filter(book -> book.getTitle().equals("Title2")).findFirst().orElse(null);

        Book dto1 = result.stream().filter(dto -> dto.getTitle().equals("Title1")).findFirst().orElse(null);
        Book dto2 = result.stream().filter(dto -> dto.getTitle().equals("Title2")).findFirst().orElse(null);

        assertNotNull(book1);
        assertNotNull(book2);
        assertNotNull(dto1);
        assertNotNull(dto2);

        // Assert book1 and dto1 details
        assertEquals(book1.getTitle(), dto1.getTitle());
        assertEquals(book1.getAuthor(), dto1.getAuthor());
        assertEquals(book1.getIsbn(), dto1.getIsbn());
        assertEquals(book1.getPublishDate(), dto1.getPublishDate());
        assertEquals(book1.getPrice(), dto1.getPrice());

        // Assert book2 and dto2 details
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
    void testUpdateBook_Success() throws Exception {
        // Arrange
        String identifier = "1";
        String type = "id";
        Book existingBook = new Book();
        Book newBookDetails = new Book();

        when(strategyFactory.getUpdateStrategy(type)).thenReturn(updateStrategy);
        when(bookRepository.getReferenceById(Integer.parseInt(identifier))).thenReturn(existingBook);

        // Act
        ResponseEntity<Void> response = bookService.updateBook(identifier, type, newBookDetails, null);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(updateStrategy, times(1)).update(existingBook, newBookDetails, null);
    }

    @Test
    void testUpdateBook_StrategyNotFound() throws IOException {
        // Arrange
        String identifier = "1";
        String type = "unknownType";
        String filename = "file.pdf";
        Book newBookDetails = new Book();

        when(strategyFactory.getUpdateStrategy(type)).thenReturn(null);

        // Act
        ResponseEntity<Void> response = bookService.updateBook(identifier, type, newBookDetails, filename);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(updateStrategy, never()).update(any(Book.class), any(Book.class), anyString());
    }

    @Test
    void testUpdateBook_BookNotFound() throws IOException {
        // Arrange
        String identifier = "1";
        String type = "id";
        String filename = "file.pdf";
        Book newBookDetails = new Book();

        when(strategyFactory.getUpdateStrategy(type)).thenReturn(updateStrategy);
        when(bookRepository.getReferenceById(Integer.parseInt(identifier))).thenThrow(new BookNotFoundException("Book not found with id: " + identifier, null));

        // Act
        ResponseEntity<Void> response = bookService.updateBook(identifier, type, newBookDetails, filename);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(updateStrategy, never()).update(any(Book.class), any(Book.class), anyString());
    }

    @Test
    void testUpdateBook_IllegalArgumentException() throws Exception {
        // Arrange
        String identifier = "abc";
        String type = "id";
        String filename = "file.pdf";
        Book existingBook = new Book();
        Book newBookDetails = new Book();

        when(strategyFactory.getUpdateStrategy(type)).thenReturn(updateStrategy);
        when(bookRepository.findById(anyInt())).thenReturn(Optional.of(existingBook));
        doThrow(new IllegalArgumentException()).when(updateStrategy).update(existingBook, newBookDetails, filename);

        // Act
        ResponseEntity<Void> response = bookService.updateBook(identifier, type, newBookDetails, filename);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testUpdateBook_IOException() throws Exception {
        // Arrange
        String identifier = "abc";
        String type = "id";
        String filename = "file.pdf";
        Book existingBook = new Book();
        Book newBookDetails = new Book();

        when(strategyFactory.getUpdateStrategy(type)).thenReturn(updateStrategy);
        when(bookRepository.findById(anyInt())).thenReturn(Optional.of(existingBook));
        doThrow(new IOException()).when(updateStrategy).update(existingBook, newBookDetails, filename);

        // Act
        ResponseEntity<Void> response = bookService.updateBook(identifier, type, newBookDetails, filename);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void deleteBook_ShouldReturnBadRequest_WhenStrategyIsNull() {
        String identifier = "123";
        String type = "isbn";

        when(strategyFactory.getDeleteStrategy(type)).thenReturn(null);

        ResponseEntity<Void> response = bookService.deleteBook(identifier, type);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void deleteBook_ShouldReturnOk_WhenBookIsDeletedSuccessfully() throws IOException {
        String identifier = "123";
        String type = "isbn";
        Book book = new Book();

        when(strategyFactory.getDeleteStrategy(type)).thenReturn(deleteStrategy);
        when(bookRepository.findBookByIsbn(identifier)).thenReturn(Optional.of(book));

        ResponseEntity<Void> response = bookService.deleteBook(identifier, type);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(deleteStrategy, times(1)).delete(book);
    }

    @Test
    void deleteBook_ShouldReturnNotFound_WhenBookNotFound() {
        String identifier = "123";
        String type = "isbn";

        when(strategyFactory.getDeleteStrategy(type)).thenReturn(deleteStrategy);
        when(bookRepository.findBookByIsbn(identifier)).thenThrow(new BookNotFoundException("Book not found with id: " + identifier, null));

        ResponseEntity<Void> response = bookService.deleteBook(identifier, type);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteBook_ShouldReturnInternalServerError_WhenIllegalArgumentExceptionIsThrown() {
        String identifier = "123";
        String type = "isbn";

        when(strategyFactory.getDeleteStrategy(type)).thenReturn(deleteStrategy);
        when(bookRepository.findBookByIsbn(identifier)).thenThrow(new IllegalArgumentException());

        ResponseEntity<Void> response = bookService.deleteBook(identifier, type);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void deleteBook_ShouldReturnInternalServerError_WhenIOExceptionIsThrown() throws IOException {
        String identifier = "123";
        String type = "isbn";
        Book book = new Book();

        when(strategyFactory.getDeleteStrategy(type)).thenReturn(deleteStrategy);
        when(bookRepository.findBookByIsbn(identifier)).thenReturn(Optional.of(book));
        doThrow(new IOException()).when(deleteStrategy).delete(book);

        ResponseEntity<Void> response = bookService.deleteBook(identifier, type);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testAddBookToCurrentUser() {
        // Arrange
        User mockUser = new User();
        mockUser.setUsername("testuser");

        Book mockBook = new Book();
        mockBook.setId(1);
        mockBook.setTitle("Test Book");

        Set<Book> books = new HashSet<>();
        mockUser.setBooks(books);

        mockCurrentUser(mockUser);

        when(bookRepository.getReferenceById(1)).thenReturn(mockBook);

        // Act
        bookService.addBookToCurrentUser("1", "id");

        // Assert
        assertTrue(mockUser.getBooks().contains(mockBook), "The book should be added to the user's collection");
        verify(userRepository).save(mockUser);
    }

    @Test
    public void testAddBookToCurrentUser_BookAlreadyInCollection() {
        // Arrange
        User mockUser = new User();
        mockUser.setUsername("testuser");

        Book mockBook = new Book();
        mockBook.setId(1);
        mockBook.setTitle("Test Book");

        Set<Book> books = new HashSet<>();
        books.add(mockBook);
        mockUser.setBooks(books);

        mockCurrentUser(mockUser);

        when(bookRepository.getReferenceById(1)).thenReturn(mockBook);

        // Act
        bookService.addBookToCurrentUser("1", "id");

        // Assert
        assertEquals(1, mockUser.getBooks().size(), "The user's collection should not have duplicate books");
        assertTrue(mockUser.getBooks().contains(mockBook), "The user's collection should contain the book");
        verify(userRepository).save(mockUser);
    }

    @Test
    public void testAddBookToCurrentUser_UserNotAuthenticated() {
        // Arrange
        mockCurrentUser(null);

        // Act and Assert
        assertThrows(NullPointerException.class, () -> {
            bookService.addBookToCurrentUser("1", "id");
        }, "Expected NullPointerException to be thrown when no user is authenticated");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testDeleteBookFromCurrentUser_Success() {
        // Arrange
        User mockUser = new User();
        mockUser.setUsername("testuser");

        Book mockBook = new Book();
        mockBook.setId(1);
        mockBook.setTitle("Test Book");

        Set<Book> books = new HashSet<>();
        books.add(mockBook);
        mockUser.setBooks(books);

        mockCurrentUser(mockUser);

        when(bookRepository.getReferenceById(1)).thenReturn(mockBook);

        // Act
        bookService.deleteBookFromCurrentUser("1", "id");

        // Assert
        assertFalse(mockUser.getBooks().contains(mockBook), "The book should be removed from the user's collection");
        verify(userRepository).save(mockUser);
    }

    @Test
    public void testDeleteBookFromCurrentUser_BookNotFoundInCollection() {
        // Arrange
        User mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setBooks(new HashSet<>());

        mockCurrentUser(mockUser);

        Book mockBook = new Book();
        mockBook.setId(1);
        when(bookRepository.getReferenceById(1)).thenReturn(mockBook);

        // Act
        ResponseEntity<Void> response = bookService.deleteBook("1", "id");

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode(), "Response status should be 404 Not Found");
        verify(userRepository, never()).save(any(User.class));
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

@AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
